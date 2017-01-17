Contributing to Eclipse Che
=====================

Before Submitting an Issue
--------------------------
Check that [our issue database](https://github.com/eclipse/che/issues)
doesn't already include that problem or suggestion before submitting an issue.
If you find a match, you can use the "subscribe" button to get notified on
updates. Do *not* leave random "+1" or "I have this too" comments, as they
only clutter the discussion, and don't help resolving it. However, if you
have ways to reproduce the issue or have additional information that may help
resolving the issue, please leave a comment.

Writing Good Bug Reports and Feature Requests
---------------------------------------------
Please file a single issue per problem and feature request. Do not file combo issues. Please do not submit multiple comments on a single issue - write your issue with all the environmental information and reproduction steps so that an engineer can reproduce it.

The community wants to help you find a solution to your problem, but every problem is unique. In order for an engineer to help resolve your issue, they need to be able to reproduce it. We put the product through extensive manual and automated QA for every release, and verify all of its functionality. Any feature that was previously working in a release that is no longer working is marked as a severity/blocker for immediate review.

This means that if you are encountering an error, it is likely due to a unique configuration of your system. Reproducing your specific error may require significant information about your system and environment. Help us in advance by providing a complete assessment of your system and the steps necessary to reproduce the issue.

Therefore:
* The details of your environment including OS version, Docker configuration, and the steps how you configure & run Che.
* Provide reproducibule steps, what the result of the steps was, and what you would have expected.
* When providing reproduction steps, start with launching Che providing its configuration and every step taken to create the problem.
* A detailed description of the behavior that you expect.
* Animated GIFs that show the behavior are often times necessary to observe the problems.
* If there are UI issues or errors, please run the steps with the browser dev console open and send those logs with your report.

Contributing Improvements
-------------------------
If you are interested in fixing issues and contributing directly to the code base, please the document [How to Contribute](https://github.com/eclipse/che/wiki/How-To-Contribute).


Dependency Repositories
-------------------------
Eclipse Che is dependent on the following external repositories to build:

[https://github.com/codenvy/che-docs](https://github.com/codenvy/che-docs)

Run `mvn clean install` for the repositories above to create required artifacts before building Che.
