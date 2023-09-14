import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Login");
    }

    async getHtml() {
        this.setAppProgress(20);
        let returnHTML = formatHTML(this.params.username);
        returnHTML = returnHTML.replaceAll("\n","");
        this.setAppProgress(80);
        return returnHTML;
    }
}

function setAppProgress(prg) {
    try {
        if (prg < 0) {
            document.getElementById("appProgress").value = 1;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg > 100) {
            document.getElementById("appProgress").value = 100;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg === 0) {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").removeAttribute("value");
        } else {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").value = prg;
        }
    } catch (e) {
        document.getElementById("appProgress").style.display = "none";
    }
}

async function postLogin(username, password) {
    setAppProgress(20);
    let formData = new FormData;
    formData.set("username", username);
    formData.set("password", password);

    const response = await fetch('/api/v1/app/login', {
        method: 'POST',
        body: formData
    });
    setAppProgress(60);
    const rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        setAppProgress(80);
        return rsp;
    } else {
        setAppProgress(80);
        return {"token": "", "firstName": "", "lastName": "", "loggedIn": false};
    }
}

async function renewToken(token) {
    setAppProgress(20);
    const response = await fetch('/api/v1/app/renewtoken', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    setAppProgress(60);
    const rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        setAppProgress(80);
        return rsp;
    } else {
        setAppProgress(80);
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