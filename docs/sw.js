const CACHE_NAME = 'reminiskinai-cache-v1';

const PRECACHE_ASSETS = [
  './',
  './index.html',
  './styles.css',
  './app.js',
  './manifest.json',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/apple-touch-icon.png',
  './icons/favicon.png',
  './assets/sample_1_before.webp',
  './assets/sample_1_remini_ref.webp',
  './assets/sample_2_before.webp',
  './assets/sample_2_remini_ref.webp',
  './assets/sample_3_before.webp',
  './assets/men_1_navy_suit.png',
  './assets/men_2_charcoal_suit.png',
  './assets/men_3_grey_blazer.png',
  './assets/men_4_black_formal.png',
  './assets/men_5_brown_open_collar.png',
  './assets/women_1_navy_blazer.png',
  './assets/women_2_black_suit.png',
  './assets/women_3_beige_blazer.png',
  './assets/women_4_burgundy_blazer.png',
  './assets/women_5_grey_blazer.png'
];

self.addEventListener('install', (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(PRECACHE_ASSETS);
    })
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => caches.delete(name))
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;

  event.respondWith(
    caches.match(event.request, { ignoreSearch: true }).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(event.request).then((networkResponse) => {
        if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
          return networkResponse;
        }
        const responseToCache = networkResponse.clone();
        caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, responseToCache);
        });
        return networkResponse;
      }).catch(() => {
        // Offline fallback to index.html for navigation requests
        if (event.request.mode === 'navigate') {
          return caches.match('./index.html');
        }
      });
    })
  );
});
