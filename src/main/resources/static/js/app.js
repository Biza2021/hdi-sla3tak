document.querySelectorAll('[data-preview-target]').forEach((input) => {
  input.addEventListener('change', (event) => {
    const targetSelector = input.getAttribute('data-preview-target');
    const preview = document.querySelector(targetSelector);
    const file = event.target.files?.[0];
    if (!preview || !file) return;
    const reader = new FileReader();
    reader.onload = (e) => preview.setAttribute('src', e.target?.result);
    reader.readAsDataURL(file);
  });
});
