import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Search");
    }

    async getHtml() {
        let searchResults = await getRemoteSearch(this.params.id);
        let returnHTML = `<h1>Search Results</h1>`;
        for (let i in searchResults) {
            let searchResult = searchResults[i];
            returnHTML += htmlResultLine(searchResult);
        }
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteSearch(searchTerm) {
    const response = await fetch('/api/v1/app/search/'+searchTerm);
    const searchResults = await response.json();
    return searchResults;
}

function htmlResultLine(rst) {
    let lnk = "\/app\/"+rst.searchType+"\/"+rst.searchId;
    let r = "";
    r+=`<div class="list__item" data-link-search="`+lnk+`">`;
    r+=`<div class="list__Item-line" data-link-search="`+lnk+`">`;
    r+=`<div class="item-id" data-link-search="`+lnk+`">`+rst.searchId+`</div>`;
    r+=`<div class="item-user" data-link-search="`+lnk+`">`+rst.searchName+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-search="`+lnk+`">`;
    r+=`<div class="item-detail" data-link-search="`+lnk+`">`+rst.searchDetail+`</div>`;
    r+=`</div></div>`;

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}