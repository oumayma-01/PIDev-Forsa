$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
# Source: official mark (public/forsa-logo.png)
$srcPath = Join-Path $PSScriptRoot '..\public\forsa-logo.png' | Resolve-Path
$public = Join-Path $PSScriptRoot '..\public' | Resolve-Path

function Export-SquareIcon([string]$outName, [int]$size) {
  $src = [System.Drawing.Image]::FromFile($srcPath)
  $bmp = New-Object System.Drawing.Bitmap $size, $size
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
  $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
  # White tile so the logo matches its intended presentation on home screens
  $bg = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)
  $g.Clear($bg)
  $pad = [Math]::Max(8, [int][Math]::Round($size * 0.06))
  $inner = $size - (2 * $pad)
  $ratio = [Math]::Min($inner / $src.Width, $inner / $src.Height)
  $w = [int][Math]::Round($src.Width * $ratio)
  $h = [int][Math]::Round($src.Height * $ratio)
  $x = [int][Math]::Round(($size - $w) / 2)
  $y = [int][Math]::Round(($size - $h) / 2)
  $g.DrawImage($src, $x, $y, $w, $h)
  $outPath = Join-Path $public $outName
  $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
  $g.Dispose()
  $bmp.Dispose()
  $src.Dispose()
  Write-Output "Wrote $outPath"
}

Export-SquareIcon 'pwa-icon-192.png' 192
Export-SquareIcon 'pwa-icon-512.png' 512
