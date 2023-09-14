import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Settings");
    }

    async getHtml() {
        this.setAppProgress(20);
        let showSubscribe = true;
        let workers = await navigator.serviceWorker.getRegistrations();
        console.log(workers.length);
        this.setAppProgress(60);
        if (workers.length > 0) {
            showSubscribe = false;
        }

        let r = `<h1>Settings</h1>`;

        r+=`<div class="settings__container"><div>`;
        r+=`<div class="settings__container-label">Account</div>`;
        r+=`<div class="settings__container-field">`+this.params.username+`</div>`;
        r+=`<div class="settings__container-label">&nbsp;</div>`;
        r+=`<div class="settings__container-field"><button class="btn btn-sm btn-wiki" data-settings-logout>Logout</button></div>`;

        r+=`<div class="settings__container-label">&nbsp;</div>`;
        r+=`<div class="settings__container-field">&nbsp;</div>`;

        r+=`<div class="settings__container-label">Theme</div>`;
        r+=`<div class="settings__container-field"><button class="btn btn-sm btn-wiki" data-settings-theme>Toggle</button></div>`;

        r+=`<div class="settings__container-label">&nbsp;</div>`;
        r+=`<div class="settings__container-field">&nbsp;</div>`;

        r+=`<div class="settings__container-label">Notifications</div>`;
        if (showSubscribe) {
            r += `<div class="settings__container-field"><button class="btn btn-sm btn-wiki" data-settings-notify-true>Subscribe</button></div>`;
        } else {
            r += `<div class="settings__container-field"><button class="btn btn-sm btn-wiki" data-settings-notify-false>Unsubscribe</button></div>`;
        }

        r+=`<div class="settings__container-label">&nbsp;</div>`;
        r+=`<div class="settings__container-field">&nbsp;</div>`;


        r+=`</div></div>`;

        this.setAppProgress(80);
        return r;
    }
}

function setAppProgress(prg) {
    try {
        if (prg < 0) {
            document.getElementById("appProgress").value = 1;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg > 100) {
            document.getElementById("appProgress").value = 100;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg === 0) {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").removeAttribute("value");
        } else {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").value = prg;
        }
    } catch (e) {
        document.getElementById("appProgress").style.display = "none";
    }
}