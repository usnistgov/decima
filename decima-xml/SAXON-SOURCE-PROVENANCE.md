# Saxon adapter source provenance

The original submodule pin, d7f239a8d53346f086706ec8a67b9e9bd4caf9bb, could not be
retrieved from the configured official Saxonica mirror or checked local caches.
The maintainer approved replacing it with the official restored-10.6 commit:

- Commit: ff1b55a28e3ac03512f998c7366012d9bda5f3fc
- Upstream: https://saxonica.plan.io/saxonica/saxonmirrorhe.he.git
- Advertised tag: he-restored-saxon10_6
- Tree: c9c3fd0094e18ba0f8c81ec092eb3f35236d85e2

This is a replacement. Equivalence to the missing commit has not been established.
The existing build copies seven JDOM2 adapter Java files from this submodule;
the Saxon-HE 10.6 and JDOM2 2.0.6.1 runtime dependency versions remain unchanged.

The seven adapter sources match, after line-ending normalization, the official
[10.6 source archive](https://raw.githubusercontent.com/Saxonica/Saxon-HE/3924b81f1e91b62ee71977b5bfaa3d09c8ef0d67/10/source/saxon10-6source.zip).
The archive SHA-256 is f4108b4608e1ccd23034cee47910c27944939604c566b3c85e75da03cd8f5639.
The archive is comparison evidence; the build continues to consume the pinned submodule.
