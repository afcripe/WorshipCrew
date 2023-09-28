import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let post = await getRemoteResourceArticle(this.params.id, this.params.token);

        let returnHTML = htmlHomePage(post, this.params.id);

        this.setAppProgress(80);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }

    async getNotification() {
        return null;
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

async function getRemoteResourceArticle(id, token) {
    let formData = new FormData();
        formData.set("name", id);

    const response = await fetch('/api/v1/app/resources/article', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    const post = await response.json();
    const status = response.status;
    return post;
}

function htmlHomePage(post, id) {
    let r =`<div class="resource__group">`;
    r+=`<div class="resource__title">`+post.title+`</div>`;
    r+=`</div>`;
    r+=`<div class="resource__group">`;
    if (post.author.fullName !== null) {
        r += `<div class="resource__subtitle">Author: ` + post.author.fullName + `</div>`;
    } else {
        r += `<div class="resource__subtitle">Author: ` + post.author.firstName + ` ` + post.author.lastName + `</div>`;
    }
    r+=`</div>`;
    r+=`<div class="resource__group">`;
        let thisLink = location.protocol+"//"+location.host+"/resource/articles/"+id;
        r += `<div class="resource__subtitle">URL: <span style="text-decoration: underline;" data-nav-copy="`+thisLink+`">` + id + `</span></div>`;
    r+=`</div>`;
    r+=`<div class="resource__group">`;
    r+=`<div class="resource__subtitle">Last Updated: `+formatDate(post.lastUpdated)+`</div>`;
    r+=`</div>`;
    r+=`<hr>`;

    r+=post.body;

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}