param(
  [Parameter(Mandatory=$true)][string]$NewVersion
)
# Update pom.xml version using Maven's built-in versioning
& mvn versions:set -DnewVersion=$NewVersion -DgenerateBackupPoms=false
# Update CHANGELOG compare link
if (Select-String -Path CHANGELOG.md -Pattern '\[Unreleased\]:') {
  (Get-Content CHANGELOG.md) -replace '(\[Unreleased\]: .*compare\/)(v\d+\.\d+\.\d+)(\.\.\.HEAD)', "$1v$NewVersion$3" | Set-Content CHANGELOG.md
}
& git add pom.xml CHANGELOG.md
& git commit -m "chore(release): v$NewVersion"
& git tag -a "v$NewVersion" -m "Release v$NewVersion"
Write-Output "Version bumped to $NewVersion. Next: push branch + tag."

