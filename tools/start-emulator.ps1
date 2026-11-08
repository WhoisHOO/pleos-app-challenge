<#
.SYNOPSIS
  Starts the Pleos Connect emulator for Docentrip as a process of its own and waits until Android has booted.

.DESCRIPTION
  The emulator keeps running after this script and its terminal are closed, so a tool that cleans up its own
  background jobs does not take the emulator with it. It boots cold (the AVD's own setting) and saves no snapshot.
  Stop it with "adb emu kill"; "adb reboot" hangs this emulator.

  With -HostMic the car hears the PC's microphone, for Docentrip's voice commands (Pleos SpeechToText). Without it the
  emulator gives the car silence, as it does by default.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\tools\start-emulator.ps1
  powershell -ExecutionPolicy Bypass -File .\tools\start-emulator.ps1 -NoWait
  powershell -ExecutionPolicy Bypass -File .\tools\start-emulator.ps1 -HostMic
#>
param(
    [string]$Avd = 'Pleos_Connect_v2.1.0',
    [string]$Sdk = "$env:LOCALAPPDATA\Android\Sdk",
    [int]$TimeoutSec = 600,
    [switch]$NoWait,
    [switch]$HostMic
)

$emulator = Join-Path $Sdk 'emulator\emulator.exe'
$adb = Join-Path $Sdk 'platform-tools\adb.exe'
foreach ($tool in @($emulator, $adb)) {
    if (-not (Test-Path $tool)) { throw "Not found: $tool (see docs/handoff.md for the setup)" }
}

$process = $null
# One emulator per AVD: a second one would refuse to start. One already running may still be booting, so it is
# waited for like a new one.
if (Get-Process -Name 'qemu-system-x86_64*' -ErrorAction SilentlyContinue) {
    Write-Host 'The emulator is already running.'
} else {
    # The AVD takes 4 GB; below about 5 GB free the PC slows down, or the emulator is stopped for memory.
    $freeGb = (Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory / 1MB
    if ($freeGb -lt 5) {
        Write-Warning ('Only {0:N1} GB of memory is free and the emulator needs about 4 GB. Close other programs first.' -f $freeGb)
    }

    $log = Join-Path $env:TEMP 'docentrip-emulator.log'
    # Hidden: the emulator's console shows nothing (its output goes to the log), and closing it would stop the emulator.
    $arguments = @('-avd', $Avd, '-no-snapshot-save', '-no-boot-anim')
    if ($HostMic) { $arguments += '-allow-host-audio' }
    $process = Start-Process -FilePath $emulator -ArgumentList $arguments `
        -RedirectStandardOutput $log -RedirectStandardError "$log.err" -WindowStyle Hidden -PassThru
    # Holding the handle keeps the exit code readable if the emulator stops.
    $null = $process.Handle
    Write-Host "Emulator started (process $($process.Id)); its output goes to $log"
}
if ($NoWait) { return }

$started = Get-Date
$deadline = $started.AddSeconds($TimeoutSec)
do {
    Start-Sleep -Seconds 5
    if ($process -and $process.HasExited) {
        Get-Content $log, "$log.err" -Tail 20 -ErrorAction SilentlyContinue
        throw "The emulator stopped while booting (exit code $($process.ExitCode))."
    }
    # -e: the emulator, even with a phone also connected to adb.
    $booted = "$(& $adb -e shell getprop sys.boot_completed 2>$null)".Trim() -eq '1'
} until ($booted -or (Get-Date) -gt $deadline)

if (-not $booted) { throw "The emulator did not finish booting within $TimeoutSec s." }
Write-Host ('Booted in {0:N0} s.' -f ((Get-Date) - $started).TotalSeconds)
