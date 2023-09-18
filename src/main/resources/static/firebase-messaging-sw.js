
self.addEventListener('notificationclick', function(event) {
    switch (event.action) {
        case 'open_url':
            clients.openWindow(event.notification.data.url);
            break;
        case 'any_other_action':
            clients.openWindow("https://www.example.com");
            break;
    }
});

self.addEventListener('push', function(e) {

    const payload = JSON.parse(e.data.text());
    console.log(payload);
    let payloadURL = payload.data.link+payload.data.module+payload.data.moduleId;

    let options = {
        title: payload.notification.title,
        body: payload.notification.body,
        icon: payload.notification.image,
        data: { url:payloadURL },
        actions: [{action: "open_url", title: "View Now"}]
    };
    e.waitUntil(
        self.registration.showNotification(payload.notification.title, options)
    );
});