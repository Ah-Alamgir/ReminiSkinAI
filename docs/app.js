/**
 * ReminiSkinAI - Web PWA Core Application
 * Ported from Kotlin Multiplatform (Compose) to high-performance client-side Web/Canvas.
 * Fully functional offline on iOS Safari & modern browsers.
 */

(() => {
  'use strict';

  // ==========================================================================
  // 1. Service Worker Registration & PWA Setup
  // ==========================================================================
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js', { scope: './' })
        .then((reg) => {
          console.log('[PWA] Service Worker registered:', reg.scope);
        })
        .catch((err) => {
          console.warn('[PWA] Service Worker registration failed:', err);
        });
    });
  }

  // Offline status badge updater
  function updateOnlineStatus() {
    const badge = document.getElementById('offlineStatusBadge');
    if (!badge) return;
    if (navigator.onLine) {
      badge.innerHTML = '<span class="status-dot"></span> Offline Ready';
      badge.style.color = '#10B981';
    } else {
      badge.innerHTML = '<span class="status-dot" style="background:#38BDF8;box-shadow:0 0 8px #38BDF8"></span> Running Offline';
      badge.style.color = '#38BDF8';
    }
  }
  window.addEventListener('online', updateOnlineStatus);
  window.addEventListener('offline', updateOnlineStatus);
  updateOnlineStatus();

  // iOS Add to Home Screen Banner detection
  const isIos = /iPhone|iPad|iPod/.test(navigator.userAgent) && !window.MSStream;
  const isStandalone = window.navigator.standalone === true || window.matchMedia('(display-mode: standalone)').matches;
  const iosBanner = document.getElementById('iosInstallBanner');
  const dismissBtn = document.getElementById('dismissIosBanner');

  if (isIos && !isStandalone && !localStorage.getItem('iosBannerDismissed')) {
    if (iosBanner) iosBanner.classList.remove('hidden');
  }
  if (dismissBtn) {
    dismissBtn.addEventListener('click', () => {
      if (iosBanner) iosBanner.classList.add('hidden');
      localStorage.setItem('iosBannerDismissed', '1');
    });
  }

  // ==========================================================================
  // 2. Constants & Presets (Mirrored from Kotlin Multiplatform)
  // ==========================================================================
  const AESTHETIC_PRESETS = [
    {
      id: 'REMINI_NATURAL',
      title: 'Remini Natural',
      subtitle: 'Signature studio clarity & healthy rosy-peach glow (Default)',
      swatch: '#F5D0C0',
      rMult: 1.02, gMult: 1.01, bMult: 1.01, radBoost: 14, contrast: 1.03
    },
    {
      id: 'AMERICAN',
      title: 'American Fair White',
      subtitle: 'Fair creamy-white porcelain with neutral undertones',
      swatch: '#FCEFE4',
      rMult: 1.02, gMult: 1.02, bMult: 1.03, radBoost: 18, contrast: 1.02
    },
    {
      id: 'KOREAN',
      title: 'Korean Glass Skin',
      subtitle: 'K-Beauty translucent ivory & luminous peach highlight',
      swatch: '#FFF1EB',
      rMult: 1.03, gMult: 1.02, bMult: 1.05, radBoost: 26, contrast: 1.02
    },
    {
      id: 'AFRICAN',
      title: 'African Deep Melanin',
      subtitle: 'Authentic rich black/ebony with velvety luster',
      swatch: '#4A3026',
      rMult: 1.00, gMult: 0.99, bMult: 0.99, radBoost: 8, contrast: 1.06
    },
    {
      id: 'BANGLADESHI',
      title: 'South Asian Golden',
      subtitle: 'Warm golden-wheat glow & rich amber undertones',
      swatch: '#C68A5E',
      rMult: 1.05, gMult: 1.03, bMult: 0.98, radBoost: 16, contrast: 1.03
    },
    {
      id: 'LATINA_OLIVE',
      title: 'Latina Olive Bronze',
      subtitle: 'Sun-kissed warm olive & radiant honey bronze',
      swatch: '#D29B6C',
      rMult: 1.06, gMult: 1.03, bMult: 0.96, radBoost: 20, contrast: 1.04
    },
    {
      id: 'NORDIC_FAIR',
      title: 'Nordic Alabaster',
      subtitle: 'Ultra-bright cool alabaster & crystal clarity',
      swatch: '#FFF5F0',
      rMult: 1.02, gMult: 1.02, bMult: 1.04, radBoost: 22, contrast: 1.02
    },
    {
      id: 'MEDITERRANEAN',
      title: 'Mediterranean Sun',
      subtitle: 'Golden warm terracotta & sunlit radiance',
      swatch: '#DC9E75',
      rMult: 1.07, gMult: 1.03, bMult: 0.95, radBoost: 18, contrast: 1.04
    }
  ];

  const PASSPORT_STANDARDS = {
    US_2X2: { widthMm: 50.8, heightMm: 50.8, wPx: 600, hPx: 600, aspect: 1.0 },
    UK_EU_35X45: { widthMm: 35, heightMm: 45, wPx: 413, hPx: 531, aspect: 35 / 45 },
    BANGLADESH_40X50: { widthMm: 40, heightMm: 50, wPx: 472, hPx: 591, aspect: 40 / 50 },
    INDIA_2X2: { widthMm: 50.8, heightMm: 50.8, wPx: 600, hPx: 600, aspect: 1.0 },
    JAPAN_35X45: { widthMm: 35, heightMm: 45, wPx: 413, hPx: 531, aspect: 35 / 45 },
    CANADA_50X70: { widthMm: 50, heightMm: 70, wPx: 591, hPx: 827, aspect: 50 / 70 },
    CHINA_33X48: { widthMm: 33, heightMm: 48, wPx: 390, hPx: 567, aspect: 33 / 48 },
    AUSTRALIA_35X45: { widthMm: 35, heightMm: 45, wPx: 413, hPx: 531, aspect: 35 / 45 },
    UAE_SAUDI_40X60: { widthMm: 40, heightMm: 60, wPx: 472, hPx: 709, aspect: 40 / 60 },
    MALAYSIA_SG_35X50: { widthMm: 35, heightMm: 50, wPx: 413, hPx: 591, aspect: 35 / 50 }
  };

  const SUIT_TEMPLATES = [
    // Men
    { id: 'men_1_navy_suit', title: 'Navy Tie Suit', gender: 'MEN', src: 'assets/men_1_navy_suit.png' },
    { id: 'men_2_charcoal_suit', title: 'Executive Charcoal', gender: 'MEN', src: 'assets/men_2_charcoal_suit.png' },
    { id: 'men_3_grey_blazer', title: 'Slate Grey Suit', gender: 'MEN', src: 'assets/men_3_grey_blazer.png' },
    { id: 'men_4_black_formal', title: 'Classic Jet Black', gender: 'MEN', src: 'assets/men_4_black_formal.png' },
    { id: 'men_5_brown_open_collar', title: 'Espresso Blazer', gender: 'MEN', src: 'assets/men_5_brown_open_collar.png' },
    // Women
    { id: 'women_1_navy_blazer', title: 'Navy V-Blazer', gender: 'WOMEN', src: 'assets/women_1_navy_blazer.png' },
    { id: 'women_2_black_suit', title: 'Black Executive', gender: 'WOMEN', src: 'assets/women_2_black_suit.png' },
    { id: 'women_3_beige_blazer', title: 'Cream Tailored', gender: 'WOMEN', src: 'assets/women_3_beige_blazer.png' },
    { id: 'women_4_burgundy_blazer', title: 'Burgundy Formal', gender: 'WOMEN', src: 'assets/women_4_burgundy_blazer.png' },
    { id: 'women_5_grey_blazer', title: 'Charcoal Lapel', gender: 'WOMEN', src: 'assets/women_5_grey_blazer.png' }
  ];

  // ==========================================================================
  // 3. Application State
  // ==========================================================================
  const state = {
    currentTab: 'main', // 'main' or 'passport'
    sourceImage: null,  // HTMLImageElement
    sourceLoaded: false,

    // Calibration settings
    selectedPresetId: 'REMINI_NATURAL',
    warmth: 1.0,
    radiance: 1.0,
    smoothness: 0.85,
    clarity: 0.82,
    aiEnhanceEnabled: true,

    // Split slider position (0 to 100%)
    splitPos: 50,

    // Passport settings
    passportStandardKey: 'US_2X2',
    passportBgColor: '#FFFFFF',
    selectedSuitId: null,
    suitFilter: 'ALL',
    suitScale: 1.0,
    suitOffsetX: 0,
    suitOffsetY: 0,
    showBiometricGuides: false,

    // Loaded suit image cache
    loadedSuits: {}
  };

  // Preload suit images into memory
  SUIT_TEMPLATES.forEach(s => {
    const img = new Image();
    img.src = s.src;
    img.onload = () => { state.loadedSuits[s.id] = img; };
  });

  // ==========================================================================
  // 4. UI Elements
  // ==========================================================================
  const tabMainBtn = document.getElementById('tabMainBtn');
  const tabPassportBtn = document.getElementById('tabPassportBtn');
  const screenMain = document.getElementById('screenMain');
  const screenPassport = document.getElementById('screenPassport');

  const imageInput = document.getElementById('imageInput');
  const cameraBtn = document.getElementById('cameraBtn');
  const comparisonContainer = document.getElementById('comparisonContainer');
  const beforeWrapper = document.getElementById('beforeWrapper');
  const splitDivider = document.getElementById('splitDivider');
  const beforeCanvas = document.getElementById('beforeCanvas');
  const afterCanvas = document.getElementById('afterCanvas');
  const processingIndicator = document.getElementById('processingIndicator');
  const toggleAiEnhanceBtn = document.getElementById('toggleAiEnhanceBtn');

  const presetsGrid = document.getElementById('presetsGrid');
  const sliderWarmth = document.getElementById('sliderWarmth');
  const sliderRadiance = document.getElementById('sliderRadiance');
  const sliderSmoothness = document.getElementById('sliderSmoothness');
  const sliderClarity = document.getElementById('sliderClarity');
  const valWarmth = document.getElementById('valWarmth');
  const valRadiance = document.getElementById('valRadiance');
  const valSmoothness = document.getElementById('valSmoothness');
  const valClarity = document.getElementById('valClarity');
  const resetSlidersBtn = document.getElementById('resetSlidersBtn');

  // Passport Elements
  const countryStandardSelect = document.getElementById('countryStandardSelect');
  const passportCanvas = document.getElementById('passportCanvas');
  const biometricGuide = document.getElementById('biometricGuide');
  const toggleGuidesBtn = document.getElementById('toggleGuidesBtn');
  const resetTransformBtn = document.getElementById('resetTransformBtn');
  const bgPalette = document.getElementById('bgPalette');
  const currentBgLabel = document.getElementById('currentBgLabel');
  const suitGrid = document.getElementById('suitGrid');
  const sliderSuitScale = document.getElementById('sliderSuitScale');
  const sliderSuitY = document.getElementById('sliderSuitY');
  const sliderSuitX = document.getElementById('sliderSuitX');
  const valSuitScale = document.getElementById('valSuitScale');
  const valSuitY = document.getElementById('valSuitY');
  const valSuitX = document.getElementById('valSuitX');
  const exportSinglePassportBtn = document.getElementById('exportSinglePassportBtn');
  const exportSheetBtn = document.getElementById('exportSheetBtn');

  // Modal Elements
  const exportModalBtn = document.getElementById('exportModalBtn');
  const exportModal = document.getElementById('exportModal');
  const closeModalBtn = document.getElementById('closeModalBtn');
  const confirmDownloadBtn = document.getElementById('confirmDownloadBtn');

  // ==========================================================================
  // 5. Navigation Tab Switcher
  // ==========================================================================
  function switchTab(tab) {
    state.currentTab = tab;
    if (tab === 'main') {
      tabMainBtn.classList.add('active');
      tabPassportBtn.classList.remove('active');
      screenMain.classList.add('active');
      screenPassport.classList.remove('active');
      renderMainPreview();
    } else {
      tabPassportBtn.classList.add('active');
      tabMainBtn.classList.remove('active');
      screenPassport.classList.add('active');
      screenMain.classList.remove('active');
      renderPassportPreview();
    }
  }

  tabMainBtn.addEventListener('click', () => switchTab('main'));
  tabPassportBtn.addEventListener('click', () => switchTab('passport'));

  // ==========================================================================
  // 6. Image Loading & Sample Handling
  // ==========================================================================
  function loadSourceImage(src) {
    showProcessing(true);
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      state.sourceImage = img;
      state.sourceLoaded = true;
      showProcessing(false);
      renderMainPreview();
      renderPassportPreview();
    };
    img.onerror = () => {
      showProcessing(false);
      alert('Failed to load image.');
    };
    img.src = src;
  }

  imageInput.addEventListener('change', (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (evt) => loadSourceImage(evt.target.result);
      reader.readAsDataURL(file);
    }
  });

  cameraBtn.addEventListener('click', () => {
    imageInput.setAttribute('capture', 'user');
    imageInput.click();
    imageInput.removeAttribute('capture');
  });

  document.querySelectorAll('.sample-chip').forEach((chip) => {
    chip.addEventListener('click', () => {
      const sample = chip.getAttribute('data-sample');
      loadSourceImage(`assets/${sample}`);
    });
  });

  function showProcessing(show) {
    if (processingIndicator) {
      processingIndicator.classList.toggle('hidden', !show);
    }
  }

  // ==========================================================================
  // 7. Core Image Processing Algorithms (SkinToneProcessor in JS)
  // ==========================================================================
  function processSkinTone(sourceImageData, width, height, preset, warmth, radiance, smoothness, clarity, aiEnhance) {
    const data = sourceImageData.data; // RGBA Uint8ClampedArray
    const totalPixels = width * height;
    const skinWeights = new Float32Array(totalPixels);

    const effWarmth = Math.min(1.4, Math.max(0.6, warmth));
    const effRMult = preset.rMult * effWarmth;
    const effGMult = preset.gMult * ((effWarmth - 1.0) * 0.25 + 1.0);
    const effBMult = Math.min(1.30, Math.max(0.70, preset.bMult / effWarmth));
    const radOffset = Math.min(35, Math.max(-25, (radiance - 1.0) * preset.radBoost));

    // STAGE 1: Human Skin Dermis Segmentation (Strict YCbCr + Chroma + Hue)
    for (let i = 0; i < totalPixels; i++) {
      const idx = i * 4;
      const r = data[idx];
      const g = data[idx + 1];
      const b = data[idx + 2];

      const maxC = Math.max(r, g, b);
      const minC = Math.min(r, g, b);
      const chromaDiff = maxC - minC;

      if (chromaDiff < 14) continue;
      if (r <= g || r <= b) continue;

      const saturation = chromaDiff / maxC;
      if (saturation > 0.68) continue;
      if (maxC < 32) continue;

      // YCbCr transformation
      const cr = 128 + 0.500 * r - 0.419 * g - 0.081 * b;
      const cb = 128 - 0.169 * r - 0.331 * g + 0.500 * b;

      if (cr < 133 || cr > 175 || cb < 80 || cb > 130) continue;

      // Hue calculation
      let hue = 0;
      if (maxC === r) {
        hue = 60 * (((g - b) / chromaDiff) % 6);
      } else if (maxC === g) {
        hue = 60 * (((b - r) / chromaDiff) + 2);
      } else {
        hue = 60 * (((r - g) / chromaDiff) + 4);
      }
      if (hue < 0) hue += 360;

      if ((hue < 0 || hue > 38) && (hue < 345 || hue > 360)) continue;

      const dCr = (cr - 150) / 18;
      const dCb = (cb - 105) / 16;
      const distSq = dCr * dCr + dCb * dCb;

      if (distSq < 1.0) {
        skinWeights[i] = Math.min(1.0, Math.max(0.0, 1.0 - distSq));
      }
    }

    // STAGE 2: Bilateral Dermis Skin Smoothing
    const smoothStrength = Math.min(1.0, Math.max(0.0, smoothness));
    const smoothed = new Uint8ClampedArray(data);

    if (smoothStrength > 0.05) {
      const blendFactor = smoothStrength * 0.65;
      for (let y = 1; y < height - 1; y++) {
        const row = y * width;
        const rowAbove = (y - 1) * width;
        const rowBelow = (y + 1) * width;

        for (let x = 1; x < width - 1; x++) {
          const idx = row + x;
          const sw = skinWeights[idx];
          if (sw > 0.20) {
            const pIdx = idx * 4;
            const cR = data[pIdx];
            const cG = data[pIdx + 1];
            const cB = data[pIdx + 2];

            let sumR = cR * 2.0;
            let sumG = cG * 2.0;
            let sumB = cB * 2.0;
            let count = 2.0;

            // Neighbors
            const neighbors = [
              rowAbove + x,
              rowBelow + x,
              row + (x - 1),
              row + (x + 1)
            ];

            for (let n = 0; n < 4; n++) {
              const nIdx = neighbors[n];
              const npIdx = nIdx * 4;
              const nR = data[npIdx];
              const nG = data[npIdx + 1];
              const nB = data[npIdx + 2];

              const diff = Math.abs(nR - cR) + Math.abs(nG - cG) + Math.abs(nB - cB);
              if (diff < 50 && skinWeights[nIdx] > 0.15) {
                const w = 1.0 - (diff / 50);
                sumR += nR * w;
                sumG += nG * w;
                sumB += nB * w;
                count += w;
              }
            }

            const blend = sw * blendFactor;
            smoothed[pIdx] = Math.round(cR * (1.0 - blend) + (sumR / count) * blend);
            smoothed[pIdx + 1] = Math.round(cG * (1.0 - blend) + (sumG / count) * blend);
            smoothed[pIdx + 2] = Math.round(cB * (1.0 - blend) + (sumB / count) * blend);
          }
        }
      }
    }

    // STAGE 3: Regional Color Multipliers & Radiance Boost
    const outData = new Uint8ClampedArray(smoothed);
    const pContrast = preset.contrast;

    for (let i = 0; i < totalPixels; i++) {
      const sw = skinWeights[i];
      if (sw <= 0) continue;

      const idx = i * 4;
      const r = smoothed[idx];
      const g = smoothed[idx + 1];
      const b = smoothed[idx + 2];

      const yLum = 0.299 * r + 0.587 * g + 0.114 * b;
      let contrastFactor = 1.0;
      if (yLum > 128) {
        contrastFactor = 1.0 + (pContrast - 1.0) * ((yLum - 128) / 128);
      } else {
        contrastFactor = 1.0 - (pContrast - 1.0) * ((128 - yLum) / 128) * 0.4;
      }

      const rGraded = Math.min(255, Math.max(0, r * effRMult * contrastFactor + radOffset));
      const gGraded = Math.min(255, Math.max(0, g * effGMult * contrastFactor + radOffset * 0.85));
      const bGraded = Math.min(255, Math.max(0, b * effBMult * contrastFactor + radOffset * 0.80));

      outData[idx] = Math.round(r * (1.0 - sw) + rGraded * sw);
      outData[idx + 1] = Math.round(g * (1.0 - sw) + gGraded * sw);
      outData[idx + 2] = Math.round(b * (1.0 - sw) + bGraded * sw);
    }

    // STAGE 4: High-frequency Detail Restoration & Micro-Texture Clarity
    if (aiEnhance || clarity > 0.1) {
      const sharpenAmount = (aiEnhance ? 0.38 : 0.0) + (clarity * 0.25);
      const enhanced = new Uint8ClampedArray(outData);

      for (let y = 1; y < height - 1; y++) {
        const row = y * width;
        const rowAbove = (y - 1) * width;
        const rowBelow = (y + 1) * width;

        for (let x = 1; x < width - 1; x++) {
          const idx = (row + x) * 4;
          const r = outData[idx];
          const g = outData[idx + 1];
          const b = outData[idx + 2];

          const topIdx = (rowAbove + x) * 4;
          const botIdx = (rowBelow + x) * 4;
          const leftIdx = (row + x - 1) * 4;
          const rightIdx = (row + x + 1) * 4;

          const avgR = (outData[topIdx] + outData[botIdx] + outData[leftIdx] + outData[rightIdx]) * 0.25;
          const avgG = (outData[topIdx + 1] + outData[botIdx + 1] + outData[leftIdx + 1] + outData[rightIdx + 1]) * 0.25;
          const avgB = (outData[topIdx + 2] + outData[botIdx + 2] + outData[leftIdx + 2] + outData[rightIdx + 2]) * 0.25;

          enhanced[idx] = Math.min(255, Math.max(0, Math.round(r + (r - avgR) * sharpenAmount)));
          enhanced[idx + 1] = Math.min(255, Math.max(0, Math.round(g + (g - avgG) * sharpenAmount)));
          enhanced[idx + 2] = Math.min(255, Math.max(0, Math.round(b + (b - avgB) * sharpenAmount)));
        }
      }
      return enhanced;
    }

    return outData;
  }

  // ==========================================================================
  // 8. Main Preview Rendering & Split Slider
  // ==========================================================================
  let renderDebounce = null;
  function scheduleRender() {
    if (renderDebounce) cancelAnimationFrame(renderDebounce);
    renderDebounce = requestAnimationFrame(() => {
      renderMainPreview();
    });
  }

  function renderMainPreview() {
    if (!state.sourceImage || !state.sourceLoaded) return;

    const img = state.sourceImage;
    // Calculate display dimensions fitting inside comparisonContainer
    const maxW = 900;
    const scale = Math.min(1.0, maxW / img.naturalWidth);
    const w = Math.round(img.naturalWidth * scale);
    const h = Math.round(img.naturalHeight * scale);

    beforeCanvas.width = w;
    beforeCanvas.height = h;
    afterCanvas.width = w;
    afterCanvas.height = h;

    // Draw Before Canvas (original image)
    const ctxBefore = beforeCanvas.getContext('2d');
    ctxBefore.drawImage(img, 0, 0, w, h);

    // Process & Draw After Canvas
    const ctxAfter = afterCanvas.getContext('2d');
    ctxAfter.drawImage(img, 0, 0, w, h);
    const imgData = ctxAfter.getImageData(0, 0, w, h);

    const preset = AESTHETIC_PRESETS.find(p => p.id === state.selectedPresetId) || AESTHETIC_PRESETS[0];
    const processedPixels = processSkinTone(
      imgData, w, h,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity,
      state.aiEnhanceEnabled
    );

    const processedImageData = new ImageData(processedPixels, w, h);
    ctxAfter.putImageData(processedImageData, 0, 0);

    updateSplitSliderPosition(state.splitPos);
  }

  function updateSplitSliderPosition(percentage) {
    state.splitPos = Math.max(0, Math.min(100, percentage));
    beforeWrapper.style.width = `${state.splitPos}%`;
    splitDivider.style.left = `${state.splitPos}%`;
  }

  // Split Divider Drag Logic (Mouse & Touch)
  let isDraggingSplit = false;
  function handleSplitDrag(e) {
    if (!isDraggingSplit) return;
    const rect = comparisonContainer.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const x = clientX - rect.left;
    const pct = (x / rect.width) * 100;
    updateSplitSliderPosition(pct);
  }

  comparisonContainer.addEventListener('mousedown', (e) => {
    isDraggingSplit = true;
    handleSplitDrag(e);
  });
  window.addEventListener('mousemove', handleSplitDrag);
  window.addEventListener('mouseup', () => { isDraggingSplit = false; });

  comparisonContainer.addEventListener('touchstart', (e) => {
    isDraggingSplit = true;
    handleSplitDrag(e);
  }, { passive: true });
  window.addEventListener('touchmove', handleSplitDrag, { passive: true });
  window.addEventListener('touchend', () => { isDraggingSplit = false; });

  // ==========================================================================
  // 9. Presets & Controls UI Wiring
  // ==========================================================================
  function renderPresets() {
    presetsGrid.innerHTML = '';
    AESTHETIC_PRESETS.forEach((preset) => {
      const card = document.createElement('div');
      card.className = `preset-card ${preset.id === state.selectedPresetId ? 'active' : ''}`;
      card.innerHTML = `
        <div class="preset-header">
          <span class="preset-swatch" style="background: ${preset.swatch};"></span>
          <span class="preset-title">${preset.title}</span>
        </div>
        <p class="preset-subtitle">${preset.subtitle}</p>
      `;
      card.addEventListener('click', () => {
        state.selectedPresetId = preset.id;
        document.querySelectorAll('.preset-card').forEach(c => c.classList.remove('active'));
        card.classList.add('active');
        scheduleRender();
      });
      presetsGrid.appendChild(card);
    });
  }

  // Sliders
  sliderWarmth.addEventListener('input', (e) => {
    state.warmth = parseFloat(e.target.value);
    valWarmth.textContent = `${state.warmth.toFixed(2)}×`;
    scheduleRender();
  });
  sliderRadiance.addEventListener('input', (e) => {
    state.radiance = parseFloat(e.target.value);
    valRadiance.textContent = `${state.radiance.toFixed(2)}×`;
    scheduleRender();
  });
  sliderSmoothness.addEventListener('input', (e) => {
    state.smoothness = parseFloat(e.target.value);
    valSmoothness.textContent = `${Math.round(state.smoothness * 100)}%`;
    scheduleRender();
  });
  sliderClarity.addEventListener('input', (e) => {
    state.clarity = parseFloat(e.target.value);
    valClarity.textContent = `${Math.round(state.clarity * 100)}%`;
    scheduleRender();
  });

  resetSlidersBtn.addEventListener('click', () => {
    state.warmth = 1.0;
    state.radiance = 1.0;
    state.smoothness = 0.85;
    state.clarity = 0.82;
    state.selectedPresetId = 'REMINI_NATURAL';

    sliderWarmth.value = '1.0';
    sliderRadiance.value = '1.0';
    sliderSmoothness.value = '0.85';
    sliderClarity.value = '0.82';
    valWarmth.textContent = '1.00×';
    valRadiance.textContent = '1.00×';
    valSmoothness.textContent = '85%';
    valClarity.textContent = '82%';

    renderPresets();
    scheduleRender();
  });

  toggleAiEnhanceBtn.addEventListener('click', () => {
    state.aiEnhanceEnabled = !state.aiEnhanceEnabled;
    toggleAiEnhanceBtn.classList.toggle('active', state.aiEnhanceEnabled);
    scheduleRender();
  });

  // ==========================================================================
  // 10. Passport Photo Studio
  // ==========================================================================
  countryStandardSelect.addEventListener('change', (e) => {
    state.passportStandardKey = e.target.value;
    renderPassportPreview();
  });

  toggleGuidesBtn.addEventListener('click', () => {
    state.showBiometricGuides = !state.showBiometricGuides;
    toggleGuidesBtn.classList.toggle('active', state.showBiometricGuides);
    biometricGuide.classList.toggle('hidden', !state.showBiometricGuides);
  });

  // Background Swatches
  bgPalette.querySelectorAll('.bg-swatch').forEach(swatch => {
    swatch.addEventListener('click', () => {
      bgPalette.querySelectorAll('.bg-swatch').forEach(s => s.classList.remove('active'));
      swatch.classList.add('active');
      state.passportBgColor = swatch.getAttribute('data-color');
      currentBgLabel.textContent = swatch.getAttribute('title');
      renderPassportPreview();
    });
  });

  // Suit Catalog Rendering
  function renderSuitCatalog() {
    suitGrid.innerHTML = '';

    // None Option
    const noneCard = document.createElement('div');
    noneCard.className = `suit-card ${state.selectedSuitId === null ? 'active' : ''}`;
    noneCard.innerHTML = `
      <div class="suit-none-icon">🚫</div>
      <span class="suit-title">No Outfit</span>
    `;
    noneCard.addEventListener('click', () => {
      state.selectedSuitId = null;
      renderSuitCatalog();
      renderPassportPreview();
    });
    suitGrid.appendChild(noneCard);

    // Suits Filtered
    const filtered = SUIT_TEMPLATES.filter(s => {
      if (state.suitFilter === 'ALL') return true;
      return s.gender === state.suitFilter;
    });

    filtered.forEach(s => {
      const card = document.createElement('div');
      card.className = `suit-card ${state.selectedSuitId === s.id ? 'active' : ''}`;
      card.innerHTML = `
        <img class="suit-thumb" src="${s.src}" alt="${s.title}">
        <span class="suit-title">${s.title}</span>
      `;
      card.addEventListener('click', () => {
        state.selectedSuitId = s.id;
        renderSuitCatalog();
        renderPassportPreview();
      });
      suitGrid.appendChild(card);
    });
  }

  document.querySelectorAll('.suit-filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.suit-filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.suitFilter = btn.getAttribute('data-filter');
      renderSuitCatalog();
    });
  });

  // Suit Adjustment Sliders
  sliderSuitScale.addEventListener('input', (e) => {
    state.suitScale = parseFloat(e.target.value);
    valSuitScale.textContent = `${Math.round(state.suitScale * 100)}%`;
    renderPassportPreview();
  });
  sliderSuitY.addEventListener('input', (e) => {
    state.suitOffsetY = parseInt(e.target.value, 10);
    valSuitY.textContent = `${state.suitOffsetY}px`;
    renderPassportPreview();
  });
  sliderSuitX.addEventListener('input', (e) => {
    state.suitOffsetX = parseInt(e.target.value, 10);
    valSuitX.textContent = `${state.suitOffsetX}px`;
    renderPassportPreview();
  });

  resetTransformBtn.addEventListener('click', () => {
    state.suitScale = 1.0;
    state.suitOffsetX = 0;
    state.suitOffsetY = 0;
    sliderSuitScale.value = '1.0';
    sliderSuitY.value = '0';
    sliderSuitX.value = '0';
    valSuitScale.textContent = '100%';
    valSuitY.textContent = '0px';
    valSuitX.textContent = '0px';
    renderPassportPreview();
  });

  // Passport Canvas Render
  function renderPassportPreview() {
    if (!state.sourceImage || !state.sourceLoaded) return;

    const std = PASSPORT_STANDARDS[state.passportStandardKey] || PASSPORT_STANDARDS.US_2X2;
    const w = std.wPx;
    const h = std.hPx;

    passportCanvas.width = w;
    passportCanvas.height = h;
    const ctx = passportCanvas.getContext('2d');

    // 1. Draw Background
    if (state.passportBgColor === 'transparent') {
      ctx.clearRect(0, 0, w, h);
    } else {
      ctx.fillStyle = state.passportBgColor;
      ctx.fillRect(0, 0, w, h);
    }

    // 2. Draw Subject / Photo fitted into standard crop
    const img = state.sourceImage;
    const imgAspect = img.naturalWidth / img.naturalHeight;
    const canvasAspect = w / h;

    let srcW = img.naturalWidth;
    let srcH = img.naturalHeight;
    let srcX = 0;
    let srcY = 0;

    if (imgAspect > canvasAspect) {
      srcW = img.naturalHeight * canvasAspect;
      srcX = (img.naturalWidth - srcW) / 2;
    } else {
      srcH = img.naturalWidth / canvasAspect;
      srcY = (img.naturalHeight - srcH) / 4; // Biometric head headroom bias
    }

    ctx.drawImage(img, srcX, srcY, srcW, srcH, 0, 0, w, h);

    // 3. Draw Formal Suit / Outfit Overlay
    if (state.selectedSuitId && state.loadedSuits[state.selectedSuitId]) {
      const suitImg = state.loadedSuits[state.selectedSuitId];
      const suitAspect = suitImg.naturalWidth / suitImg.naturalHeight;

      const baseSuitW = w * 1.05 * state.suitScale;
      const baseSuitH = baseSuitW / suitAspect;

      const posX = (w - baseSuitW) / 2 + state.suitOffsetX;
      const posY = (h - baseSuitH * 0.78) + state.suitOffsetY; // Collar positioned on chest

      ctx.drawImage(suitImg, posX, posY, baseSuitW, baseSuitH);
    }
  }

  // ==========================================================================
  // 11. Export & Download Functions
  // ==========================================================================
  exportModalBtn.addEventListener('click', () => {
    if (!state.sourceLoaded) {
      alert('Please upload a photo first.');
      return;
    }
    exportModal.classList.remove('hidden');
  });

  closeModalBtn.addEventListener('click', () => {
    exportModal.classList.add('hidden');
  });

  confirmDownloadBtn.addEventListener('click', () => {
    const resMode = document.querySelector('input[name="exportRes"]:checked').value;
    const fmt = document.querySelector('input[name="exportFormat"]:checked').value;
    exportEnhancedPhoto(resMode, fmt);
    exportModal.classList.add('hidden');
  });

  function exportEnhancedPhoto(resolutionMode, format) {
    if (!state.sourceImage) return;

    const img = state.sourceImage;
    let targetW = img.naturalWidth;
    let targetH = img.naturalHeight;

    if (resolutionMode === '4k') {
      const scale = Math.max(3840 / targetW, 2160 / targetH);
      targetW = Math.round(targetW * scale);
      targetH = Math.round(targetH * scale);
    }

    const expCanvas = document.createElement('canvas');
    expCanvas.width = targetW;
    expCanvas.height = targetH;
    const ctx = expCanvas.getContext('2d');
    ctx.drawImage(img, 0, 0, targetW, targetH);

    const imgData = ctx.getImageData(0, 0, targetW, targetH);
    const preset = AESTHETIC_PRESETS.find(p => p.id === state.selectedPresetId) || AESTHETIC_PRESETS[0];

    const processed = processSkinTone(
      imgData, targetW, targetH,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity,
      state.aiEnhanceEnabled
    );

    ctx.putImageData(new ImageData(processed, targetW, targetH), 0, 0);

    const mime = format === 'jpeg' ? 'image/jpeg' : 'image/png';
    const ext = format === 'jpeg' ? 'jpg' : 'png';
    const quality = format === 'jpeg' ? 0.98 : undefined;

    const url = expCanvas.toDataURL(mime, quality);
    triggerDownload(url, `ReminiSkinAI_Enhanced_${Date.now()}.${ext}`);
  }

  // Single 300 DPI Passport Export
  exportSinglePassportBtn.addEventListener('click', () => {
    if (!state.sourceLoaded) {
      alert('Please upload a photo first.');
      return;
    }
    renderPassportPreview();
    const url = passportCanvas.toDataURL('image/jpeg', 0.98);
    triggerDownload(url, `Biometric_Passport_${state.passportStandardKey}_${Date.now()}.jpg`);
  });

  // 4x6" Print Sheet (6 Copies with cut lines)
  exportSheetBtn.addEventListener('click', () => {
    if (!state.sourceLoaded) {
      alert('Please upload a photo first.');
      return;
    }
    renderPassportPreview();

    // 4x6 inch at 300 DPI = 1800 x 1200 px (Landscape)
    const sheetCanvas = document.createElement('canvas');
    sheetCanvas.width = 1800;
    sheetCanvas.height = 1200;
    const sCtx = sheetCanvas.getContext('2d');

    // Clean white photo lab paper
    sCtx.fillStyle = '#FFFFFF';
    sCtx.fillRect(0, 0, 1800, 1200);

    // 2 rows × 3 columns (6 copies)
    const cols = 3;
    const rows = 2;
    const pW = passportCanvas.width;
    const pH = passportCanvas.height;

    // Scale passport photo to fit cell with margin
    const cellW = 1800 / cols;
    const cellH = 1200 / rows;
    const maxTargetW = cellW * 0.88;
    const maxTargetH = cellH * 0.88;
    const fitScale = Math.min(maxTargetW / pW, maxTargetH / pH);
    const destW = pW * fitScale;
    const destH = pH * fitScale;

    for (let r = 0; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        const cx = c * cellW + (cellW - destW) / 2;
        const cy = r * cellH + (cellH - destH) / 2;

        // Draw Passport Photo
        sCtx.drawImage(passportCanvas, cx, cy, destW, destH);

        // Thin cut guide border around each photo
        sCtx.strokeStyle = 'rgba(160, 160, 160, 0.45)';
        sCtx.lineWidth = 1;
        sCtx.strokeRect(cx, cy, destW, destH);
      }
    }

    // Sheet label & cut guides
    sCtx.fillStyle = '#94A3B8';
    sCtx.font = '20px -apple-system, sans-serif';
    sCtx.fillText('ReminiSkinAI — Standard 4×6" (10×15 cm) 300 DPI Biometric Lab Print Sheet', 40, 1175);

    const url = sheetCanvas.toDataURL('image/jpeg', 0.98);
    triggerDownload(url, `Passport_4x6_PrintSheet_${Date.now()}.jpg`);
  });

  function triggerDownload(dataUrl, filename) {
    const a = document.createElement('a');
    a.href = dataUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  // ==========================================================================
  // 12. Initialization
  // ==========================================================================
  renderPresets();
  renderSuitCatalog();
  // Automatically load first sample portrait
  loadSourceImage('assets/sample_1_before.webp');

})();
