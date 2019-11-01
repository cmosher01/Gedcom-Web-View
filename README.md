# Gedcom-Web-View

**GEDCOM Web View** serves GEDCOM files as web pages.


Copyright © 2004–2019, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Gedcom-Lib.svg)](https://www.gnu.org/licenses/gpl.html)

## Usage

Requires Java JDK version 8 (or higher) installed. Build and install manually:

```shell script
./gradlew build
sudo tar xf build/distributions/gedcom-web-view-1.2.0.tar -C /opt
```

Set up the directory `./gedcom/` to contain GEDCOM (`*.ged`) files to serve, then run the start script:

```shell script
/opt/gedcom-web-view-1.2.0/bin/gedcom-web-view
```

Browse to `http://localhost:4567/`.

## Privacy

GEDCOM files typically contain private information of recent events. **GEDCOM Web View** will hide such sensitive information,
except for authorized users who sign in using their Google account. To allow access to private information for a user,
add their email address to a file named `SERVE_PUBLIC_GED_FILES` in the same directory as the `*.ged` files. For example: 

```shell script
echo "uncle.vito@gmail.com" >SERVE_PUBLIC_GED_FILES
```

Set up [Google OAuth 2.0 web application credentials](https://console.developers.google.com/apis/credentials)
for your web site, and add your web URLs as Authorized JavaScript origins and redirect URIs. Pass the Client
ID in the `CLIENT_ID` environment variable. For example:

```shell script
CLIENT_ID=1234567890-afnrivao9wr89y3jnwr.apps.googleusercontent.com /opt/gedcom-web-view-1.2.0/bin/gedcom-web-view
```
