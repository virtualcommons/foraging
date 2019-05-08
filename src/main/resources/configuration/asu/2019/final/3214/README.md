
Each directory represents the treatment sequence. The treatments are:

1 = aggressive bot, high resource growth rate.
2 = aggressive bot, low resource growth rate.
3 = nice bot, high resource growth rate.
4 = nice bot, low resource growth rate.

To run a particular treatment, copy all the .xml files in a given treatment directory into the src/main/resources/configuration directory before running an `ant deploy`. Alternatively you can use the convenience `ant deploy-fmri <treatment-dir>` command to automatically deploy the treatment configuration, e.g., `ant deploy-fmri 1432`.

