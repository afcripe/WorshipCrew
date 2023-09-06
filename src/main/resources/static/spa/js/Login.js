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
    let formData = new FormData;
        formData.set("username", username);
        formData.set("password", password);

    const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        body: formData
    });
    const rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        return rsp;
    } else {
        return {"token": "", "firstName": "", "lastName": ""};
    }
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