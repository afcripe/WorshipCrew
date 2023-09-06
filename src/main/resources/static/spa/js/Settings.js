import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Settings");
    }

    async getHtml() {
        let r = `<h1>Settings</h1>`;

        r+=`<div class="settings__container"><div>`;
        r+=`<div class="settings__container-label">Account</div>`;
        r+=`<div class="settings__container-field">`+this.params.username+`</div>`;

        r+=`<div class="settings__container-label">Notifications</div>`;
        r+=`<div class="settings__container-field"><button class="btn btn-sm btn-wiki btn-mobile" disabled>Subscribe</button></div>`;

        r+=`<div class="settings__container-label">&nbsp;</div>`;
        r+=`<div class="settings__container-field"><button class="btn btn-sm btn-wiki btn-mobile" data-settings-logout>Logout</button></div>`;

        r+=`</div></div>`;
        return r;
    }
}