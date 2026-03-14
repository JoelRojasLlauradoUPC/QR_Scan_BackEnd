param(
    [string]$InputJson = "src/main/resources/db/input/tickets.raw.json",
    [string]$OutputSql = "src/main/resources/db/seed.sql"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path $InputJson)) {
    throw "No existe el JSON de entrada: $InputJson"
}

$tickets = Get-Content -Raw -Encoding UTF8 -Path $InputJson | ConvertFrom-Json
if ($tickets -eq $null -or $tickets.Count -eq 0) {
    throw "El JSON no contiene entradas"
}

# Validacion minima para evitar seeds rotos.
$requiredFields = @("nombre", "apellido", "correo_electronico", "tipo", "pmr", "hash", "numero_local")
foreach ($ticket in $tickets) {
    foreach ($field in $requiredFields) {
        if (-not $ticket.PSObject.Properties.Name.Contains($field)) {
            throw "Falta el campo obligatorio '$field' en una entrada del JSON"
        }
    }
}

function Escape-SqlString([string]$value) {
    if ($null -eq $value) { return "" }
    return $value.Replace("'", "''")
}

$groupedByHash = @{}
foreach ($ticket in $tickets) {
    $hash = [string]$ticket.hash
    if ($groupedByHash.ContainsKey($hash)) {
        throw "Hash duplicado en el JSON: $hash"
    }
    $groupedByHash[$hash] = $ticket
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("SET NAMES utf8mb4;") | Out-Null
$lines.Add("USE qr_app;") | Out-Null
$lines.Add("") | Out-Null
$lines.Add("INSERT INTO event_tickets (nombre, apellido, correo_electronico, tipo, pmr, hash, numero_local)") | Out-Null
$lines.Add("VALUES") | Out-Null

$ordered = $tickets | Sort-Object hash
for ($i = 0; $i -lt $ordered.Count; $i++) {
    $t = $ordered[$i]

    $nombre = Escape-SqlString([string]$t.nombre)
    $apellido = Escape-SqlString([string]$t.apellido)
    $correo = Escape-SqlString([string]$t.correo_electronico)
    $tipo = [int]$t.tipo
    $pmr = if ([bool]$t.pmr) { "TRUE" } else { "FALSE" }
    $hash = Escape-SqlString([string]$t.hash)
    $numeroLocal = [int]$t.numero_local

    $row = "    ('$nombre', '$apellido', '$correo', $tipo, $pmr, '$hash', $numeroLocal)"
    if ($i -lt $ordered.Count - 1) {
        $row += ","
    } else {
        $row += ""
    }
    $lines.Add($row) | Out-Null
}

$lines.Add("ON DUPLICATE KEY UPDATE") | Out-Null
$lines.Add("    nombre = VALUES(nombre),") | Out-Null
$lines.Add("    apellido = VALUES(apellido),") | Out-Null
$lines.Add("    correo_electronico = VALUES(correo_electronico),") | Out-Null
$lines.Add("    tipo = VALUES(tipo),") | Out-Null
$lines.Add("    pmr = VALUES(pmr),") | Out-Null
$lines.Add("    numero_local = VALUES(numero_local);") | Out-Null

$encoding = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines((Resolve-Path -Path (Split-Path -Path $OutputSql -Parent)).Path + "\" + (Split-Path -Path $OutputSql -Leaf), $lines, $encoding)

Write-Host "Seed SQL generado en: $OutputSql"
Write-Host "Entradas procesadas:" $ordered.Count



