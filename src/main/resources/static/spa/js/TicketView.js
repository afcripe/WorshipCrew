import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Ticket");
    }

    async getHtml() {
        return `
            <h1>Ticket</h1>
            <p>`+this.params.id+`</p>
        `;
    }
}