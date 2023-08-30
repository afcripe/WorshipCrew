import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor() {
        super();
        this.setTitle("Tickets");
    }

    async getHtml() {
        return `
            <h1>Tickets</h1>
            <p>Lorem Ispum.</p>
        `;
    }
}