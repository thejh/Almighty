This is an android application for granting root access to other apps.
To work, it needs the su binary from https://github.com/thejh/almighty_su
to be installed as a setuid binary in the PATH.

Design
======
The only other Superuser app does a few things different - here are
the reasons why this app does stuff different.

In general, the codebase of this app should be as small and simple as
possible to reduce the attack surface and increase maintainability.

Volatile permission decisions
-----------------------------
ChainsDD's Superuser allows users to grant root access only once or
permanently. As an app that has obtained root access once can persist
this root access anyway (e.g. by dropping a setuid executable in /system),
this is only of limited use. However, it means that there must be two
code paths for determining whether permissions should be granted, increasing
attack surface.

Fine-grained access control
---------------------------
ChainsDD's Superuser grants or denies access for combinations of caller
UID, wanted UID and command. However, all apps that use `su` just request
full root access and specify `/system/bin/su` as command so that they
don't have to obtain access for every seperate command. Therefore, this
app grants access solely based on the caller's UID.

Logfiles
--------
ChainsDD's Superuser manages logfiles about permission requests. However,
as soon as an application has obtained root access, it can alter those
logfiles, so they're not very useful. Also, logfiles need some kind of database
with purging logic, so they create attack surface.

Architecture
============
When an app uses `su` for the first time, `su` will use an intent to
launch `thejh.almighty.AskPermissionActivity`. This activity asks the
user whether he wants to grant full access to the device and store
the result in `/data/data/thejh.almighty/uid_ok/uid:<uid>` as one
character (`A` for "allow" and `D` for "deny"). Afterwards, it sends
`SIGKILL` to a child of `su`, thereby notifying `su` that it should
expect a result to be present.

Reporting Bugs
==============
Non-security bugs should be reported at
https://github.com/thejh/Almighty/issues (or at
https://github.com/thejh/almighty_su/issues if they mainly affect
the `su` binary). For security-relevant bugs, please contact me at
jannhorn@googlemail.com.