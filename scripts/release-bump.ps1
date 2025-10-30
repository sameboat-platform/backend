param(
  [Parameter(Mandatory=$true)][string]$NewVersion
)
# Update pom.xml version
(Get-Content pom.xml) -replace '(<version>)\d+\.\d+\.\d+(</version>)', "$1$NewVersion$2" | Set-Content pom.xml
# Update CHANGELOG compare link
if (Select-String -Path CHANGELOG.md -Pattern '\[Unreleased\]:') {
  (Get-Content CHANGELOG.md) -replace '(\[Unreleased\]: .*compare\/)(v\d+\.\d+\.\d+)(\.\.\.HEAD)', "$1v$NewVersion$3" | Set-Content CHANGELOG.md
}
& git add pom.xml CHANGELOG.md
& git commit -m "chore(release): v$NewVersion"
& git tag -a "v$NewVersion" -m "Release v$NewVersion"
Write-Output "Version bumped to $NewVersion. Next: push branch + tag."

