import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Request");
    }

    async getHtml() {
        return `
            <h1>Request</h1>
            <p>1</p>
        `;
    }
}