import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Login");
    }

    async getHtml() {
        let returnHTML = formatHTML();
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML;
    }

}


async function postLogin(username, password) {
    console.log("username: "+username);
    console.log("password: "+password);
    return true;
}

function formatHTML() {
    return `
            <h1>Login</h1>
            <form>
            <div class="form-content">
                <input id="formLogin-username" type="text" class="form-control" placeholder="E-mail">
                <input id="formLogin-password" type="password" class="form-control" placeholder="Password">
                <button type="submit" class="btn btn-sm btn-wiki" data-form-submit="formLogin">Login</button>
            </div>
            </form>
        `;
}

export { postLogin };