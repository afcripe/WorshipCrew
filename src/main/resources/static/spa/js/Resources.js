import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let folderList = await getRemoteResourceHomeFolders(this.params.token);

        let returnHTML = htmlHomePage(folderList);

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

async function getRemoteResourceHomeFolders(token) {
    const response = await fetch('/api/v1/app/resources/homefolders', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const folderList = await response.json();
    const status = response.status;
    return folderList;
}

function htmlHomePage(folderList) {
    let r =`<div class="resource__group resource__bottom-spacer">`;
    r+=`<div class="resource__title">Topics</div>`;
    r+=`</div>`;

    for (let f in folderList) {
        let folder = folderList[f];
        r+=`<div class="ticket__detail-group">`;
            r+=`<h4 data-resource-link-folder="/`+folder.folder+`">`+capitalizeFirstLetter(folder.folder)+`</h4>`;

        for (let p in folder.wikiPost) {
            let post = folder.wikiPost[p];
            r += `<div class="article-topic-details" data-resource-link-post="`+post.id+`">`;
            //r += `<div>`;
                r += `<div class="article-topic-title" data-resource-link-post="`+post.id+`">` + post.title + `</div>`;
                r += `<div class="article-topic-summary" data-resource-link-post="`+post.id+`">` + post.summary + `</div>`;
            r += `</div>`
            //r += `</div>`;
        }

        for (let t in folder.subFolders) {
            let topic = folder.subFolders[t];
            r += `<div class="article-topic-details">`;
            //r += `<div>`;
            r += `<div class="article-topic-folder" data-resource-link-folder="`+topic.folder+`">` + topic.folder + `</div>`;
            r += `</div>`
            //r += `</div>`;
        }

        r+=`</div>`;
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

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}