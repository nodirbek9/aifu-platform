// Sets the theme class before the first paint, so a dark theme never flashes light.
// A file of its own (not inline) so the Content-Security-Policy can forbid inline scripts.
;(function () {
  try {
    var stored = localStorage.getItem('aifu-theme')
    var dark =
      stored === 'dark' ||
      ((stored === null || stored === 'system') && window.matchMedia('(prefers-color-scheme: dark)').matches)
    if (dark) document.documentElement.classList.add('dark')
  } catch (e) {
    /* localStorage unavailable — the default theme applies */
  }
})()
