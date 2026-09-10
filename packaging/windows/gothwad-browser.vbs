Set WshShell = CreateObject("WScript.Shell")
strPath = WScript.Arguments(0)
WshShell.Run """" & strPath & """", 0, False
Set WshShell = Nothing
