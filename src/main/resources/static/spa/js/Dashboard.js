import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Dashboard");
    }

    async getHtml() {
        let r=`<h1>Dashboard</h1>`;
        r+=`<p>Welcome, `+this.params.firstName+`.</p>`;
        return r;
    }
}