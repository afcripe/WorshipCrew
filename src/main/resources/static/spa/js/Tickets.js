import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        return `
            <h1>Tickets</h1>
            <div>
                <a href="/app/ticket/St-000001" data-link>St-000001</a>
            </div>
            <div>
                <a href="/app/ticket/St-000002" data-link>St-000002</a>
            </div>
            <div>
                <a href="/app/ticket/St-000003" data-link>St-000003</a>
            </div>
            <div>
                <a href="/app/ticket/St-000004" data-link>St-000004</a>
            </div>
        `;
    }
}