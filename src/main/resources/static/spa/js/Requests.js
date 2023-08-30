import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor() {
        super();
        this.setTitle("Requests");
    }

    async getHtml() {
        return `
            <h1>Requests</h1>
            <p>Lorem Ispum.</p>
        `;
    }
}