import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Ticket");
    }

    async getHtml() {
        console.log(this.params.id);
        return `
            <h1>Ticket</h1>
            <p>ST-000000</p>
        `;
    }
}