import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let folder = await getRemoteResourceFolder(this.params.id, this.params.token);
        let returnHTML = htmlHomePage(folder);

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

async function getRemoteResourceFolder(id, token) {
    let formData = new FormData();
        formData.set("name", id);
    const response = await fetch('/api/v1/app/resources/folder', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    const folder = await response.json();
    const status = response.status;
    return folder;
}

function htmlHomePage(folder) {
    let r =`<div class="resource__group">`;
    r+=`<h4>`+formatFolder(folder.folder)+`</h4>`;
    r+=`</div>`;

    for (let p in folder.wikiPost) {
        let post = folder.wikiPost[p];
        r += `<div class="article-topic-details" data-resource-link-post="`+post.id+`">`;
        r += `<div class="article-topic-title" data-resource-link-post="`+post.id+`">` + post.title + `</div>`;
        r += `<div class="article-topic-summary" data-resource-link-post="`+post.id+`">` + post.summary + `</div>`;
        r += `</div>`
    }

    for (let t in folder.subFolders) {
        let topic = folder.subFolders[t];
        r += `<div class="article-topic-details">`;
        r += `<div class="article-topic-folder" data-resource-link-folder="`+topic.folder+`">` + topic.folder + `</div>`;
        r += `</div>`
    }

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}

function formatFolder(string) {
    let strArray = string.split("/");
    let strReturn = "";
    for (let i in strArray) {
        if (i>1) {
            strReturn+=" / ";
        }
        if ( strArray[i].length > 0 ) {
            strReturn+=capitalizeFirstLetter(strArray[i]);
        }
    }

    return strReturn;
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}