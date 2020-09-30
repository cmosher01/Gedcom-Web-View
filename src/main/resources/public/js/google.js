function onSignIn(googleUser) {
    var c = Cookies.get('idtoken');
    if (c === undefined) {
        document.cookie = "idtoken="+googleUser.getAuthResponse().id_token+" ; Path=/ ; Max-Age=900";
        window.location.reload(true);
    }
}

function signOut() {
    gapi.auth2.getAuthInstance().signOut();
    document.cookie = "idtoken= ; Path=/ ; Expires = Thu, 01 Jan 1970 00:00:00 GMT";
    window.location.reload(true);
}

function renderButton() {
    gapi.signin2.render(
        'gedcom-web-view-google-signin',
        {
            'scope': 'profile email',
            'width': 120,
            'height': 26,
            'onsuccess': onSignIn,
            'onfailure': signOut
        }
    );
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('signout').addEventListener('click', signOut);
});
