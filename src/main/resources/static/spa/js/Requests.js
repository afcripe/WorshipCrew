import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Requests");
    }

    async getHtml() {
        return `
            <h1>Requests</h1>
            <p>Lorem Ispum.</p>
        `;
    }
}