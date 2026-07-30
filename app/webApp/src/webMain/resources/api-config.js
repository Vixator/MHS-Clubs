(() => {
  const configured = document.querySelector('meta[name="mhs-clubs-api-base-url"]')?.content?.trim();
  const localHost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

  // Production defaults to a same-origin reverse proxy. Set the meta tag when the API has its own host.
  window.mhsClubsApiBaseUrl = configured || (localHost ? 'http://localhost:8080' : window.location.origin);
})();
