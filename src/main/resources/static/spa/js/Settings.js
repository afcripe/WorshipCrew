import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Settings");
    }

    async getHtml() {
        let r = `<h1>Settings</h1>`;
        r+=`<div>`+this.params.username+` <button class="btn btn-sm btn-wiki" data-settings-logout>Logout</button></div>`;
        return r;
    }
}