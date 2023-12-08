
self.addEventListener('notificationclick', event => {
    console.log(event)
});


self.addEventListener('install', event => {
    console.log('notification sw installing…');
    event.waitUntil(
        console.log("sw installed.")
    );
});

self.addEventListener('activate', event => {
    console.log('sw now ready to receive notifications.');
});

self.addEventListener('notificationclick', function(event) {
    console.log(event.notification.data.url);
    clients.openWindow(event.notification.data.url);
});

self.addEventListener('push', function(e) {

    const payload = JSON.parse(e.data.text());
    let payloadURL = payload.data.link;
    if (payload.data.module !== "") {
        payloadURL += "/"+payload.data.module;
    }if (payload.data.moduleId !== "") {
        payloadURL += "/"+payload.data.moduleId;
    }

    let options = {
        title: payload.notification.title,
        body: payload.notification.body,
        icon: payload.notification.image,
        data: { url:payloadURL }
    };
    e.waitUntil(
        self.registration.showNotification(payload.notification.title, options)
    );
});