<#
.SYNOPSIS
  Drives the Pleos Connect emulator's GPS along Roadio's demo route (Gwanghwamun to N Seoul Tower), for testing the
  app's "차량 위치" mode. The emulator's location service, and through it Pleos FusedLocation, follows these fixes.

.EXAMPLE
  .\tools\drive-demo-route.ps1                 # 50 km/h, one fix a second
  .\tools\drive-demo-route.ps1 -SpeedKmh 80
#>
param(
    [double]$SpeedKmh = 50,
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Route = (Join-Path $PSScriptRoot '..\app\src\main\assets\route.json')
)

# The route the app ships with, so the GPS drive always follows the app's own points (latitude, longitude).
$routeJson = Get-Content -Raw -Encoding UTF8 $Route | ConvertFrom-Json
$points = @($routeJson.points | ForEach-Object { , @([double]$_.lat, [double]$_.lon) })
if ($points.Count -lt 2) { throw "The route in $Route needs at least two points." }

function Distance-M($a, $b) {
    $r = 6371000.0
    $lat1 = $a[0] * [Math]::PI / 180; $lat2 = $b[0] * [Math]::PI / 180
    $dLat = $lat2 - $lat1; $dLon = ($b[1] - $a[1]) * [Math]::PI / 180
    $h = [Math]::Sin($dLat / 2) * [Math]::Sin($dLat / 2) + [Math]::Cos($lat1) * [Math]::Cos($lat2) * [Math]::Sin($dLon / 2) * [Math]::Sin($dLon / 2)
    return 2 * $r * [Math]::Asin([Math]::Sqrt($h))
}

$step = $SpeedKmh / 3.6
$knots = $SpeedKmh / 1.852
$carry = 0.0
for ($i = 0; $i -lt $points.Count - 1; $i++) {
    $a = $points[$i]; $b = $points[$i + 1]
    $length = Distance-M $a $b
    $along = $carry
    while ($along -lt $length) {
        $t = $along / $length
        $lat = $a[0] + ($b[0] - $a[0]) * $t
        $lon = $a[1] + ($b[1] - $a[1]) * $t
        # geo fix takes longitude first; the last two values are satellites and speed in knots.
        & $Adb emu geo fix ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0:F6}", $lon)) ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0:F6}", $lat)) 30 8 ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0:F1}", $knots)) | Out-Null
        Write-Host ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "segment {0}  {1:F5}, {2:F5}", $i + 1, $lat, $lon))
        Start-Sleep -Seconds 1
        $along += $step
    }
    $carry = $along - $length
}
$last = $points[$points.Count - 1]
& $Adb emu geo fix ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0:F6}", $last[1])) ([string]::Format([Globalization.CultureInfo]::InvariantCulture, "{0:F6}", $last[0])) 30 8 0 | Out-Null
Write-Host "arrived at the end of the route"
