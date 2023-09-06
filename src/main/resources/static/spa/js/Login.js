import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Login");
    }

    async getHtml() {
        let returnHTML = formatHTML(this.params.username);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML;
    }

}


async function postLogin(username, password) {
    let formData = new FormData;
        formData.set("username", username);
        formData.set("password", password);

    const response = await fetch('/api/v1/app/login', {
        method: 'POST',
        body: formData
    });
    const rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        return rsp;
    } else {
        return {"token": "", "firstName": "", "lastName": "", "loggedIn": false};
    }
}

async function renewToken(token) {
    const response = await fetch('/api/v1/app/renewtoken', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        return rsp;
    } else {
        return {"token": "", "firstName": "", "lastName": "", "loggedIn": false};
    }
}

function formatHTML(username) {
    return ` <h1>Login</h1>
            <form>
            <div class="form-content">
                <input id="formLogin-username" type="text" class="form-control" value="`+username+`" placeholder="E-mail">
                <input id="formLogin-password" type="password" class="form-control" placeholder="Password">
                <button type="submit" class="btn btn-sm btn-wiki" data-form-submit="formLogin">Login</button>
            </div>
            </form>`;
}

export { postLogin, renewToken };