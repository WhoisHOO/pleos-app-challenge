<#
.SYNOPSIS
  Says a voice command to Roadio on the emulator without a microphone: Windows speaks it into a recording, and a debug
  build of Roadio hears that recording through the car's speech engine (Pleos SpeechToText, on-device).

.DESCRIPTION
  For a PC whose microphone the emulator cannot hear. The text is spoken by a Korean Windows voice (Microsoft Heami,
  part of the Korean language pack) into 16 kHz, 16-bit mono PCM, copied into Roadio's own folder with run-as (debug
  builds only) and handed to Roadio, which treats it like a turn of listening after a tap on 말로 하기.
  Watch the result with: adb logcat -s Roadio

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\tools\say-command.ps1 '다시 들려줘'
#>
param(
    [Parameter(Mandatory = $true, Position = 0)][string]$Text,
    [string]$Sdk = "$env:LOCALAPPDATA\Android\Sdk",
    [int]$User = 10,
    [string]$Package = 'app.roadio'
)

$adb = Join-Path $Sdk 'platform-tools\adb.exe'
if (-not (Test-Path $adb)) { throw "Not found: $adb (see docs/handoff.md for the setup)" }

Add-Type -AssemblyName System.Speech
$voice = New-Object System.Speech.Synthesis.SpeechSynthesizer
$korean = $voice.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -eq 'ko-KR' } | Select-Object -First 1
if (-not $korean) { throw 'No Korean Windows voice. Add the Korean language pack with its speech feature in the Windows settings.' }
$voice.SelectVoice($korean.VoiceInfo.Name)

$wav = Join-Path $env:TEMP 'roadio-command.wav'
$pcm = Join-Path $env:TEMP 'roadio-command.pcm'
$format = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(16000, [System.Speech.AudioFormat.AudioBitsPerSample]::Sixteen, [System.Speech.AudioFormat.AudioChannel]::Mono)
$voice.SetOutputToWaveFile($wav, $format)
$voice.Speak($Text)
$voice.Dispose()

# Raw PCM: the samples of the WAV file's data chunk, with half a second of silence on both sides so the engine hears
# where the words begin and end.
$bytes = [System.IO.File]::ReadAllBytes($wav)
$offset = 12
$data = $null
while ($offset + 8 -le $bytes.Length) {
    $id = [System.Text.Encoding]::ASCII.GetString($bytes, $offset, 4)
    $size = [System.BitConverter]::ToInt32($bytes, $offset + 4)
    if ($id -eq 'data') { $data = $bytes[($offset + 8)..($offset + 7 + $size)]; break }
    $offset += 8 + $size + ($size % 2)
}
if (-not $data) { throw "No audio in $wav" }
$silence = New-Object byte[] 16000
[System.IO.File]::WriteAllBytes($pcm, [byte[]]($silence + $data + $silence))

& $adb push $pcm /data/local/tmp/roadio-command.pcm | Out-Null
& $adb shell run-as $Package --user $User cp /data/local/tmp/roadio-command.pcm files/command.pcm
if ($LASTEXITCODE -ne 0) { throw 'Could not copy the recording: run-as needs a debug build of Roadio.' }
& $adb shell am broadcast --user $User -a "$Package.debug.HEAR" -p $Package --es file command.pcm | Out-Null
Write-Host "Said to Roadio: $Text"
