function imageDialog(src) {
    let dialog=document.createElement("dialog");
        dialog.id="imageViewer";
        dialog.classList.add("image-viewer__dialog");
        dialog.addEventListener("click", (event) => {
            event.preventDefault();
            document.getElementById("imageViewer").remove();
        });

    let imgDiv = document.createElement("div");
        imgDiv.classList.add("image-image-viewer__div");

    let image=document.createElement("img");
        image.src=src;
        image.classList.add("image-viewer__image");

    imgDiv.appendChild(image);
    dialog.appendChild(imgDiv);
    document.getElementById("app").appendChild(dialog);

    dialog.showModal();
}

export { imageDialog };