import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Login");
    }

    async getHtml() {
        return `
            <h1>Login</h1>
            <div class="form-content">
                <input type="text" class="form-control" placeholder="E-mail">
                <input type="password" class="form-control" placeholder="Password">
                <button type="button" class="btn btn-sm btn-wiki">Login</button>
            </div>
        `;
    }
}