/**
 * Aura AI - ReminiSkinAI 4K Web PWA Core
 * Exact match of Kotlin Multiplatform Compose UI & Design Tokens
 */

(() => {
  'use strict';

  // ==========================================================================
  // 1. Service Worker & PWA Setup
  // ==========================================================================
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js', { scope: './' })
        .then((reg) => console.log('[PWA] Registered:', reg.scope))
        .catch((err) => console.warn('[PWA] Registration failed:', err));
    });
  }

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
    currentScreen: 'HOME', // HOME, COMPARISON_STUDIO, RETOUCH_SUITE, PASSPORT_STUDIO, PROCESSING, SAVED_SHARE
    targetScreenAfterProcessing: 'COMPARISON_STUDIO',
    userCredits: 48,

    // Image Buffers
    sourceImage: null,
    sourceBeforeSrc: 'assets/sample_1_before.webp',
    sourceAfterSrc: 'assets/sample_1_remini_ref.webp',
    sourceLoaded: false,
    savedDataUrl: null,

    // Retouch & Calibration
    selectedPresetId: 'REMINI_NATURAL',
    warmth: 1.0,
    radiance: 1.0,
    smoothness: 0.85,
    clarity: 0.82,

    // Studio Interactions
    splitPos: 50,
    zoomFactor: 1.0,
    isHoldActive: false,

    // Passport Studio
    passportStandardKey: 'US_2X2',
    passportBgColor: '#FFFFFF',
    selectedSuitId: null,
    suitFilter: 'ALL',
    suitScale: 1.0,
    suitOffsetX: 0,
    suitOffsetY: 0,
    showBiometricGuides: false,

    loadedSuits: {}
  };

  // Preload suit images
  SUIT_TEMPLATES.forEach(s => {
    const img = new Image();
    img.src = s.src;
    img.onload = () => { state.loadedSuits[s.id] = img; };
  });

  // ==========================================================================
  // 4. DOM Elements
  // ==========================================================================
  // Views
  const viewHome = document.getElementById('viewHome');
  const viewComparison = document.getElementById('viewComparison');
  const viewRetouch = document.getElementById('viewRetouch');
  const viewPassport = document.getElementById('viewPassport');
  const viewProcessing = document.getElementById('viewProcessing');
  const viewSaved = document.getElementById('viewSaved');
  const mainBottomNav = document.getElementById('mainBottomNav');
  const globalImageInput = document.getElementById('globalImageInput');

  // Home Elements
  const creditsBtn = document.getElementById('creditsBtn');
  const creditsCountText = document.getElementById('creditsCountText');
  const settingsBtn = document.getElementById('settingsBtn');
  const heroEnhanceBtn = document.getElementById('heroEnhanceBtn');
  const viewAllToolsBtn = document.getElementById('viewAllToolsBtn');
  const recentHistoryBtn = document.getElementById('recentHistoryBtn');

  // Studio Elements
  const studioExitBtn = document.getElementById('studioExitBtn');
  const studioSkinToneBtn = document.getElementById('studioSkinToneBtn');
  const hudSwatchMini = document.getElementById('hudSwatchMini');
  const hudPresetTitle = document.getElementById('hudPresetTitle');
  const studioZoomBtn = document.getElementById('studioZoomBtn');
  const hudZoomText = document.getElementById('hudZoomText');
  const studioHoldBtn = document.getElementById('studioHoldBtn');
  const studioRetouchShortcutBtn = document.getElementById('studioRetouchShortcutBtn');
  const studioCanvasContainer = document.getElementById('studioCanvasContainer');
  const canvasBefore = document.getElementById('canvasBefore');
  const canvasAfter = document.getElementById('canvasAfter');
  const canvasBeforeWrapper = document.getElementById('canvasBeforeWrapper');
  const studioDivider = document.getElementById('studioDivider');
  const dockIntensitySlider = document.getElementById('dockIntensitySlider');
  const dockIntensityVal = document.getElementById('dockIntensityVal');
  const studioRegenerateBtn = document.getElementById('studioRegenerateBtn');
  const studioExportBtn = document.getElementById('studioExportBtn');

  // Retouch Elements
  const retouchBackBtn = document.getElementById('retouchBackBtn');
  const retouchExportBtn = document.getElementById('retouchExportBtn');
  const retouchCanvas = document.getElementById('retouchCanvas');
  const retouchPresetsContainer = document.getElementById('retouchPresetsContainer');
  const subtabTonesContent = document.getElementById('subtabTonesContent');
  const subtabCalibrationContent = document.getElementById('subtabCalibrationContent');
  const rSliderWarmth = document.getElementById('rSliderWarmth');
  const rSliderRadiance = document.getElementById('rSliderRadiance');
  const rSliderSmoothness = document.getElementById('rSliderSmoothness');
  const rSliderClarity = document.getElementById('rSliderClarity');
  const rValWarmth = document.getElementById('rValWarmth');
  const rValRadiance = document.getElementById('rValRadiance');
  const rValSmoothness = document.getElementById('rValSmoothness');
  const rValClarity = document.getElementById('rValClarity');

  // Passport Elements
  const passportBackBtn = document.getElementById('passportBackBtn');
  const passportGuideToggleBtn = document.getElementById('passportGuideToggleBtn');
  const passportCanvas = document.getElementById('passportCanvas');
  const passportGuideOverlay = document.getElementById('passportGuideOverlay');
  const passportStandardSelect = document.getElementById('passportStandardSelect');
  const passportBgRow = document.getElementById('passportBgRow');
  const passportBgName = document.getElementById('passportBgName');
  const passportSuitCarousel = document.getElementById('passportSuitCarousel');
  const pSliderCoatScale = document.getElementById('pSliderCoatScale');
  const pSliderCoatY = document.getElementById('pSliderCoatY');
  const pSliderCoatX = document.getElementById('pSliderCoatX');
  const pValCoatScale = document.getElementById('pValCoatScale');
  const pValCoatY = document.getElementById('pValCoatY');
  const pValCoatX = document.getElementById('pValCoatX');
  const passportExportSingleBtn = document.getElementById('passportExportSingleBtn');
  const passportExportSheetBtn = document.getElementById('passportExportSheetBtn');

  // Processing Elements
  const procStageStep = document.getElementById('procStageStep');
  const procStageTitle = document.getElementById('procStageTitle');
  const procStageDesc = document.getElementById('procStageDesc');
  const procProgressFill = document.getElementById('procProgressFill');
  const procCancelBtn = document.getElementById('procCancelBtn');

  // Saved Elements
  const savedResultImg = document.getElementById('savedResultImg');
  const savedFileName = document.getElementById('savedFileName');
  const savedShareBtn = document.getElementById('savedShareBtn');
  const savedAnotherBtn = document.getElementById('savedAnotherBtn');

  // Modals
  const modalExport4k = document.getElementById('modalExport4k');
  const closeExportModalBtn = document.getElementById('closeExportModalBtn');
  const modalConfirmSaveBtn = document.getElementById('modalConfirmSaveBtn');
  const modalCredits = document.getElementById('modalCredits');
  const closeCreditsModalBtn = document.getElementById('closeCreditsModalBtn');
  const claimBonusCreditsBtn = document.getElementById('claimBonusCreditsBtn');
  const creditsDialogBalance = document.getElementById('creditsDialogBalance');
  const modalSettings = document.getElementById('modalSettings');
  const closeSettingsModalBtn = document.getElementById('closeSettingsModalBtn');
  const modalSkinTones = document.getElementById('modalSkinTones');
  const closeSkinTonesModalBtn = document.getElementById('closeSkinTonesModalBtn');
  const modalTonesGrid = document.getElementById('modalTonesGrid');

  // ==========================================================================
  // 5. Screen Navigation Router
  // ==========================================================================
  function navigateTo(screen) {
    state.currentScreen = screen;
    const views = [viewHome, viewComparison, viewRetouch, viewPassport, viewProcessing, viewSaved];
    views.forEach(v => v.classList.remove('active'));

    // Show/hide bottom nav bar based on mode (like in Compose UI)
    if (screen === 'HOME') {
      viewHome.classList.add('active');
      mainBottomNav.style.display = 'flex';
      updateNavHighlight('HOME');
    } else if (screen === 'COMPARISON_STUDIO') {
      viewComparison.classList.add('active');
      mainBottomNav.style.display = 'none';
      renderStudioCanvas();
    } else if (screen === 'RETOUCH_SUITE') {
      viewRetouch.classList.add('active');
      mainBottomNav.style.display = 'none';
      renderRetouchCanvas();
    } else if (screen === 'PASSPORT_STUDIO') {
      viewPassport.classList.add('active');
      mainBottomNav.style.display = 'none';
      renderPassportCanvas();
    } else if (screen === 'PROCESSING') {
      viewProcessing.classList.add('active');
      mainBottomNav.style.display = 'none';
    } else if (screen === 'SAVED_SHARE') {
      viewSaved.classList.add('active');
      mainBottomNav.style.display = 'none';
    }
  }

  function updateNavHighlight(navKey) {
    document.querySelectorAll('.nav-item').forEach(item => {
      item.classList.toggle('active', item.getAttribute('data-nav') === navKey);
    });
  }

  // Bottom Navigation Clicks
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => {
      const navKey = item.getAttribute('data-nav');
      if (navKey === 'HOME') {
        navigateTo('HOME');
      } else if (navKey === 'STUDIO') {
        startProcessingPipeline(state.sourceBeforeSrc, state.sourceAfterSrc, 'COMPARISON_STUDIO');
      } else if (navKey === 'RETOUCH') {
        startProcessingPipeline(state.sourceBeforeSrc, state.sourceAfterSrc, 'RETOUCH_SUITE');
      } else if (navKey === 'PASSPORT') {
        navigateTo('PASSPORT_STUDIO');
      }
    });
  });

  // ==========================================================================
  // 6. Processing Pipeline (5-Stage Simulation matching Kotlin Multiplatform)
  // ==========================================================================
  const PIPELINE_STAGES = [
    { step: 'Stage 1 of 5', title: 'Face Reconstruction v4.2', desc: 'Upscaling facial canvas to Ultra 4K UHD...', pct: 20 },
    { step: 'Stage 2 of 5', title: 'BiSeNet 19-Class Facial Parsing', desc: 'Segmenting forehead, cheeks, eyes, and lips...', pct: 45 },
    { step: 'Stage 3 of 5', title: 'Scratch Removal & Denoise', desc: 'Eliminating film grain, scratches & night noise...', pct: 68 },
    { step: 'Stage 4 of 5', title: 'GPEN-BFR-256 GAN Texture Engine', desc: 'Reconstructing high-frequency pores, iris catchlights...', pct: 88 },
    { step: 'Stage 5 of 5', title: 'Zero-DCE + YCrCb Studio Radiance', desc: 'Synthesizing studio lighting & regional tones...', pct: 100 }
  ];

  let pipelineTimer = null;
  function startProcessingPipeline(beforeSrc, afterSrc, targetMode, presetId) {
    if (presetId) state.selectedPresetId = presetId;
    state.sourceBeforeSrc = beforeSrc;
    state.sourceAfterSrc = afterSrc;
    state.targetScreenAfterProcessing = targetMode;

    navigateTo('PROCESSING');

    let stageIdx = 0;
    function runNextStage() {
      if (state.currentScreen !== 'PROCESSING') return;
      if (stageIdx >= PIPELINE_STAGES.length) {
        // Complete
        loadImagesThenNavigate(beforeSrc, afterSrc, targetMode);
        return;
      }
      const st = PIPELINE_STAGES[stageIdx];
      procStageStep.textContent = st.step;
      procStageTitle.textContent = st.title;
      procStageDesc.textContent = st.desc;
      procProgressFill.style.width = `${st.pct}%`;
      stageIdx++;
      pipelineTimer = setTimeout(runNextStage, 220);
    }
    runNextStage();
  }

  function loadImagesThenNavigate(beforeSrc, afterSrc, targetMode) {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      state.sourceImage = img;
      state.sourceLoaded = true;
      navigateTo(targetMode);
    };
    img.onerror = () => {
      navigateTo('HOME');
      alert('Failed to load image.');
    };
    img.src = afterSrc || beforeSrc;
  }

  procCancelBtn.addEventListener('click', () => {
    if (pipelineTimer) clearTimeout(pipelineTimer);
    navigateTo('HOME');
  });

  // ==========================================================================
  // 7. Core SkinToneProcessor (Identical Math to Kotlin)
  // ==========================================================================
  function runSkinToneGrading(sourceData, width, height, preset, warmth, radiance, smoothness, clarity) {
    const data = sourceData.data;
    const totalPixels = width * height;
    const skinWeights = new Float32Array(totalPixels);

    const effWarmth = Math.min(1.4, Math.max(0.6, warmth));
    const effRMult = preset.rMult * effWarmth;
    const effGMult = preset.gMult * ((effWarmth - 1.0) * 0.25 + 1.0);
    const effBMult = Math.min(1.30, Math.max(0.70, preset.bMult / effWarmth));
    const radOffset = Math.min(35, Math.max(-25, (radiance - 1.0) * preset.radBoost));

    // STAGE 1: Strict Dermis Segmentation
    for (let i = 0; i < totalPixels; i++) {
      const idx = i * 4;
      const r = data[idx];
      const g = data[idx + 1];
      const b = data[idx + 2];

      const maxC = Math.max(r, g, b);
      const minC = Math.min(r, g, b);
      const chromaDiff = maxC - minC;

      if (chromaDiff < 14 || r <= g || r <= b) continue;
      if ((chromaDiff / maxC) > 0.68 || maxC < 32) continue;

      const cr = 128 + 0.500 * r - 0.419 * g - 0.081 * b;
      const cb = 128 - 0.169 * r - 0.331 * g + 0.500 * b;
      if (cr < 133 || cr > 175 || cb < 80 || cb > 130) continue;

      let hue = 0;
      if (maxC === r) hue = 60 * (((g - b) / chromaDiff) % 6);
      else if (maxC === g) hue = 60 * (((b - r) / chromaDiff) + 2);
      else hue = 60 * (((r - g) / chromaDiff) + 4);
      if (hue < 0) hue += 360;

      if ((hue < 0 || hue > 38) && (hue < 345 || hue > 360)) continue;

      const dCr = (cr - 150) / 18;
      const dCb = (cb - 105) / 16;
      const distSq = dCr * dCr + dCb * dCb;
      if (distSq < 1.0) skinWeights[i] = 1.0 - distSq;
    }

    // STAGE 2: Bilateral Smoothing
    const smoothed = new Uint8ClampedArray(data);
    const smoothStrength = Math.min(1.0, Math.max(0.0, smoothness));
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
            let sumR = cR * 2.0, sumG = cG * 2.0, sumB = cB * 2.0, count = 2.0;
            const neighbors = [rowAbove + x, rowBelow + x, row + x - 1, row + x + 1];

            for (let n = 0; n < 4; n++) {
              const npIdx = neighbors[n] * 4;
              const nR = data[npIdx], nG = data[npIdx + 1], nB = data[npIdx + 2];
              const diff = Math.abs(nR - cR) + Math.abs(nG - cG) + Math.abs(nB - cB);
              if (diff < 50 && skinWeights[neighbors[n]] > 0.15) {
                const w = 1.0 - diff / 50;
                sumR += nR * w; sumG += nG * w; sumB += nB * w; count += w;
              }
            }
            const blend = sw * blendFactor;
            smoothed[pIdx] = Math.round(cR * (1 - blend) + (sumR / count) * blend);
            smoothed[pIdx + 1] = Math.round(cG * (1 - blend) + (sumG / count) * blend);
            smoothed[pIdx + 2] = Math.round(cB * (1 - blend) + (sumB / count) * blend);
          }
        }
      }
    }

    // STAGE 3: Color Grading & Contrast
    const outData = new Uint8ClampedArray(smoothed);
    const pContrast = preset.contrast;
    for (let i = 0; i < totalPixels; i++) {
      const sw = skinWeights[i];
      if (sw <= 0) continue;
      const idx = i * 4;
      const r = smoothed[idx], g = smoothed[idx + 1], b = smoothed[idx + 2];
      const yLum = 0.299 * r + 0.587 * g + 0.114 * b;
      const contrastFactor = yLum > 128
        ? 1.0 + (pContrast - 1.0) * ((yLum - 128) / 128)
        : 1.0 - (pContrast - 1.0) * ((128 - yLum) / 128) * 0.4;

      const rGraded = Math.min(255, Math.max(0, r * effRMult * contrastFactor + radOffset));
      const gGraded = Math.min(255, Math.max(0, g * effGMult * contrastFactor + radOffset * 0.85));
      const bGraded = Math.min(255, Math.max(0, b * effBMult * contrastFactor + radOffset * 0.80));

      outData[idx] = Math.round(r * (1 - sw) + rGraded * sw);
      outData[idx + 1] = Math.round(g * (1 - sw) + gGraded * sw);
      outData[idx + 2] = Math.round(b * (1 - sw) + bGraded * sw);
    }

    // STAGE 4: Micro-Clarity Sharpening
    if (clarity > 0.05) {
      const sharpenAmount = 0.38 + (clarity * 0.25);
      const enhanced = new Uint8ClampedArray(outData);
      for (let y = 1; y < height - 1; y++) {
        const row = y * width;
        const rowAbove = (y - 1) * width;
        const rowBelow = (y + 1) * width;
        for (let x = 1; x < width - 1; x++) {
          const idx = (row + x) * 4;
          const r = outData[idx], g = outData[idx + 1], b = outData[idx + 2];
          const top = (rowAbove + x) * 4, bot = (rowBelow + x) * 4;
          const left = (row + x - 1) * 4, right = (row + x + 1) * 4;

          const avgR = (outData[top] + outData[bot] + outData[left] + outData[right]) * 0.25;
          const avgG = (outData[top + 1] + outData[bot + 1] + outData[left + 1] + outData[right + 1]) * 0.25;
          const avgB = (outData[top + 2] + outData[bot + 2] + outData[left + 2] + outData[right + 2]) * 0.25;

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
  // 8. Studio Canvas Rendering & Interactions
  // ==========================================================================
  function renderStudioCanvas() {
    if (!state.sourceImage || !state.sourceLoaded) return;
    const img = state.sourceImage;

    const maxW = 900;
    const scale = Math.min(1.0, maxW / img.naturalWidth) * state.zoomFactor;
    const w = Math.round(img.naturalWidth * scale);
    const h = Math.round(img.naturalHeight * scale);

    canvasBefore.width = w;
    canvasBefore.height = h;
    canvasAfter.width = w;
    canvasAfter.height = h;

    const ctxB = canvasBefore.getContext('2d');
    ctxB.drawImage(img, 0, 0, w, h);

    const ctxA = canvasAfter.getContext('2d');
    ctxA.drawImage(img, 0, 0, w, h);

    const imgData = ctxA.getImageData(0, 0, w, h);
    const preset = AESTHETIC_PRESETS.find(p => p.id === state.selectedPresetId) || AESTHETIC_PRESETS[0];

    const processed = runSkinToneGrading(
      imgData, w, h,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity
    );

    ctxA.putImageData(new ImageData(processed, w, h), 0, 0);

    // Update HUD Pill
    hudSwatchMini.style.background = preset.swatch;
    hudPresetTitle.textContent = preset.title;

    updateSplitSliderPos(state.isHoldActive ? 100 : state.splitPos);
  }

  function updateSplitSliderPos(pct) {
    const val = Math.max(0, Math.min(100, pct));
    canvasBeforeWrapper.style.width = `${val}%`;
    studioDivider.style.left = `${val}%`;
  }

  // Split Dragging
  let isDraggingSplit = false;
  function handleSplitMove(clientX) {
    const rect = studioCanvasContainer.getBoundingClientRect();
    const x = clientX - rect.left;
    const pct = (x / rect.width) * 100;
    state.splitPos = pct;
    updateSplitSliderPos(pct);
  }

  studioCanvasContainer.addEventListener('mousedown', (e) => {
    isDraggingSplit = true;
    handleSplitMove(e.clientX);
  });
  window.addEventListener('mousemove', (e) => {
    if (isDraggingSplit) handleSplitMove(e.clientX);
  });
  window.addEventListener('mouseup', () => { isDraggingSplit = false; });

  studioCanvasContainer.addEventListener('touchstart', (e) => {
    isDraggingSplit = true;
    handleSplitMove(e.touches[0].clientX);
  }, { passive: true });
  window.addEventListener('touchmove', (e) => {
    if (isDraggingSplit) handleSplitMove(e.touches[0].clientX);
  }, { passive: true });
  window.addEventListener('touchend', () => { isDraggingSplit = false; });

  // Zoom Toggle (100% / 200%)
  studioZoomBtn.addEventListener('click', () => {
    state.zoomFactor = state.zoomFactor > 1.2 ? 1.0 : 1.9;
    hudZoomText.textContent = state.zoomFactor > 1.2 ? '200%' : '100%';
    studioZoomBtn.style.background = state.zoomFactor > 1.2 ? 'rgba(139, 92, 246, 0.35)' : 'rgba(255, 255, 255, 0.08)';
    renderStudioCanvas();
  });

  // Hold to Compare
  function setHoldCompare(active) {
    state.isHoldActive = active;
    updateSplitSliderPos(active ? 100 : state.splitPos);
    studioHoldBtn.style.background = active ? 'var(--aura-secondary-container)' : 'rgba(255, 255, 255, 0.08)';
  }
  studioHoldBtn.addEventListener('mousedown', () => setHoldCompare(true));
  studioHoldBtn.addEventListener('mouseup', () => setHoldCompare(false));
  studioHoldBtn.addEventListener('mouseleave', () => setHoldCompare(false));
  studioHoldBtn.addEventListener('touchstart', (e) => { e.preventDefault(); setHoldCompare(true); });
  studioHoldBtn.addEventListener('touchend', () => setHoldCompare(false));

  // Exit Studio
  studioExitBtn.addEventListener('click', () => navigateTo('HOME'));

  // Retouch Shortcut
  studioRetouchShortcutBtn.addEventListener('click', () => navigateTo('RETOUCH_SUITE'));

  // Studio Intensity Slider
  dockIntensitySlider.addEventListener('input', (e) => {
    const val = parseInt(e.target.value, 10);
    state.clarity = val / 100;
    dockIntensityVal.textContent = `${val}%`;
    renderStudioCanvas();
  });

  studioRegenerateBtn.addEventListener('click', () => {
    startProcessingPipeline(state.sourceBeforeSrc, state.sourceAfterSrc, 'COMPARISON_STUDIO');
  });

  studioExportBtn.addEventListener('click', () => {
    modalExport4k.classList.remove('hidden');
  });

  // ==========================================================================
  // 9. Retouch Suite Rendering
  // ==========================================================================
  function renderRetouchCanvas() {
    if (!state.sourceImage || !state.sourceLoaded) return;
    const img = state.sourceImage;
    const maxW = 600;
    const scale = Math.min(1.0, maxW / img.naturalWidth);
    const w = Math.round(img.naturalWidth * scale);
    const h = Math.round(img.naturalHeight * scale);

    retouchCanvas.width = w;
    retouchCanvas.height = h;
    const ctx = retouchCanvas.getContext('2d');
    ctx.drawImage(img, 0, 0, w, h);

    const imgData = ctx.getImageData(0, 0, w, h);
    const preset = AESTHETIC_PRESETS.find(p => p.id === state.selectedPresetId) || AESTHETIC_PRESETS[0];

    const processed = runSkinToneGrading(
      imgData, w, h,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity
    );
    ctx.putImageData(new ImageData(processed, w, h), 0, 0);
  }

  // Populate Retouch Presets
  function populateRetouchPresets() {
    retouchPresetsContainer.innerHTML = '';
    AESTHETIC_PRESETS.forEach(p => {
      const card = document.createElement('div');
      card.className = `preset-card-aura ${p.id === state.selectedPresetId ? 'active' : ''}`;
      card.innerHTML = `
        <div class="preset-aura-header">
          <span class="preset-swatch-aura" style="background:${p.swatch};"></span>
          <span class="preset-aura-title">${p.title}</span>
        </div>
        <p class="preset-aura-sub">${p.subtitle}</p>
      `;
      card.addEventListener('click', () => {
        state.selectedPresetId = p.id;
        document.querySelectorAll('.preset-card-aura').forEach(c => c.classList.remove('active'));
        card.classList.add('active');
        renderRetouchCanvas();
      });
      retouchPresetsContainer.appendChild(card);
    });
  }

  retouchBackBtn.addEventListener('click', () => navigateTo('HOME'));
  retouchExportBtn.addEventListener('click', () => modalExport4k.classList.remove('hidden'));

  // Retouch Sub-tabs
  document.querySelectorAll('.retouch-subtab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.retouch-subtab').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const tab = btn.getAttribute('data-subtab');
      if (tab === 'TONES') {
        subtabTonesContent.style.display = 'flex';
        subtabCalibrationContent.style.display = 'none';
      } else {
        subtabTonesContent.style.display = 'none';
        subtabCalibrationContent.style.display = 'flex';
      }
    });
  });

  // Retouch Calibration Sliders
  rSliderWarmth.addEventListener('input', (e) => {
    state.warmth = parseFloat(e.target.value);
    rValWarmth.textContent = `${state.warmth.toFixed(2)}×`;
    renderRetouchCanvas();
  });
  rSliderRadiance.addEventListener('input', (e) => {
    state.radiance = parseFloat(e.target.value);
    rValRadiance.textContent = `${state.radiance.toFixed(2)}×`;
    renderRetouchCanvas();
  });
  rSliderSmoothness.addEventListener('input', (e) => {
    state.smoothness = parseFloat(e.target.value);
    rValSmoothness.textContent = `${Math.round(state.smoothness * 100)}%`;
    renderRetouchCanvas();
  });
  rSliderClarity.addEventListener('input', (e) => {
    state.clarity = parseFloat(e.target.value);
    rValClarity.textContent = `${Math.round(state.clarity * 100)}%`;
    renderRetouchCanvas();
  });

  // ==========================================================================
  // 10. Passport Studio Rendering & Outfits
  // ==========================================================================
  function renderPassportCanvas() {
    if (!state.sourceImage || !state.sourceLoaded) return;
    const std = PASSPORT_STANDARDS[state.passportStandardKey] || PASSPORT_STANDARDS.US_2X2;
    const w = std.wPx;
    const h = std.hPx;

    passportCanvas.width = w;
    passportCanvas.height = h;
    const ctx = passportCanvas.getContext('2d');

    // 1. Background Fill
    if (state.passportBgColor === 'transparent') {
      ctx.clearRect(0, 0, w, h);
    } else {
      ctx.fillStyle = state.passportBgColor;
      ctx.fillRect(0, 0, w, h);
    }

    // 2. Draw Portrait
    const img = state.sourceImage;
    const imgAspect = img.naturalWidth / img.naturalHeight;
    const canvasAspect = w / h;
    let sW = img.naturalWidth, sH = img.naturalHeight, sX = 0, sY = 0;

    if (imgAspect > canvasAspect) {
      sW = img.naturalHeight * canvasAspect;
      sX = (img.naturalWidth - sW) / 2;
    } else {
      sH = img.naturalWidth / canvasAspect;
      sY = (img.naturalHeight - sH) / 4;
    }
    ctx.drawImage(img, sX, sY, sW, sH, 0, 0, w, h);

    // 3. Draw Suit Overlay
    if (state.selectedSuitId && state.loadedSuits[state.selectedSuitId]) {
      const suit = state.loadedSuits[state.selectedSuitId];
      const suitAspect = suit.naturalWidth / suit.naturalHeight;
      const bW = w * 1.05 * state.suitScale;
      const bH = bW / suitAspect;
      const pX = (w - bW) / 2 + state.suitOffsetX;
      const pY = (h - bH * 0.78) + state.suitOffsetY;
      ctx.drawImage(suit, pX, pY, bW, bH);
    }
  }

  function populatePassportSuits() {
    passportSuitCarousel.innerHTML = '';

    // None Option
    const noneCard = document.createElement('div');
    noneCard.className = `suit-card-item ${state.selectedSuitId === null ? 'active' : ''}`;
    noneCard.innerHTML = `<span style="font-size:26px;margin:20px 0;">🚫</span><span class="suit-name-text">No Outfit</span>`;
    noneCard.addEventListener('click', () => {
      state.selectedSuitId = null;
      populatePassportSuits();
      renderPassportCanvas();
    });
    passportSuitCarousel.appendChild(noneCard);

    const filtered = SUIT_TEMPLATES.filter(s => state.suitFilter === 'ALL' || s.gender === state.suitFilter);
    filtered.forEach(s => {
      const card = document.createElement('div');
      card.className = `suit-card-item ${state.selectedSuitId === s.id ? 'active' : ''}`;
      card.innerHTML = `
        <img src="${s.src}" alt="${s.title}" class="suit-thumb-img">
        <span class="suit-name-text">${s.title}</span>
      `;
      card.addEventListener('click', () => {
        state.selectedSuitId = s.id;
        populatePassportSuits();
        renderPassportCanvas();
      });
      passportSuitCarousel.appendChild(card);
    });
  }

  passportBackBtn.addEventListener('click', () => navigateTo('HOME'));

  passportGuideToggleBtn.addEventListener('click', () => {
    state.showBiometricGuides = !state.showBiometricGuides;
    passportGuideOverlay.classList.toggle('hidden', !state.showBiometricGuides);
    passportGuideToggleBtn.style.background = state.showBiometricGuides ? 'rgba(76, 215, 246, 0.25)' : 'rgba(255, 255, 255, 0.08)';
  });

  passportStandardSelect.addEventListener('change', (e) => {
    state.passportStandardKey = e.target.value;
    renderPassportCanvas();
  });

  passportBgRow.querySelectorAll('.bg-circle').forEach(btn => {
    btn.addEventListener('click', () => {
      passportBgRow.querySelectorAll('.bg-circle').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.passportBgColor = btn.getAttribute('data-color');
      passportBgName.textContent = btn.getAttribute('title');
      renderPassportCanvas();
    });
  });

  document.querySelectorAll('.gender-chip').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.gender-chip').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.suitFilter = btn.getAttribute('data-filter');
      populatePassportSuits();
    });
  });

  pSliderCoatScale.addEventListener('input', (e) => {
    state.suitScale = parseFloat(e.target.value);
    pValCoatScale.textContent = `${Math.round(state.suitScale * 100)}%`;
    renderPassportCanvas();
  });
  pSliderCoatY.addEventListener('input', (e) => {
    state.suitOffsetY = parseInt(e.target.value, 10);
    pValCoatY.textContent = `${state.suitOffsetY}px`;
    renderPassportCanvas();
  });
  pSliderCoatX.addEventListener('input', (e) => {
    state.suitOffsetX = parseInt(e.target.value, 10);
    pValCoatX.textContent = `${state.suitOffsetX}px`;
    renderPassportCanvas();
  });

  // Single Passport Download
  passportExportSingleBtn.addEventListener('click', () => {
    renderPassportCanvas();
    const url = passportCanvas.toDataURL('image/jpeg', 0.98);
    const fname = `Biometric_Passport_${state.passportStandardKey}_${Date.now()}.jpg`;
    showSavedScreen(url, fname);
  });

  // 4x6" Sheet Download
  passportExportSheetBtn.addEventListener('click', () => {
    renderPassportCanvas();
    const sheetCanvas = document.createElement('canvas');
    sheetCanvas.width = 1800;
    sheetCanvas.height = 1200;
    const sCtx = sheetCanvas.getContext('2d');
    sCtx.fillStyle = '#FFFFFF';
    sCtx.fillRect(0, 0, 1800, 1200);

    const cols = 3, rows = 2;
    const pW = passportCanvas.width, pH = passportCanvas.height;
    const cellW = 1800 / cols, cellH = 1200 / rows;
    const scale = Math.min((cellW * 0.88) / pW, (cellH * 0.88) / pH);
    const dW = pW * scale, dH = pH * scale;

    for (let r = 0; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        const cx = c * cellW + (cellW - dW) / 2;
        const cy = r * cellH + (cellH - dH) / 2;
        sCtx.drawImage(passportCanvas, cx, cy, dW, dH);
        sCtx.strokeStyle = 'rgba(160, 160, 160, 0.45)';
        sCtx.lineWidth = 1;
        sCtx.strokeRect(cx, cy, dW, dH);
      }
    }
    sCtx.fillStyle = '#94A3B8';
    sCtx.font = '20px -apple-system, sans-serif';
    sCtx.fillText('ReminiSkinAI — Standard 4×6" (10×15 cm) 300 DPI Biometric Lab Print Sheet', 40, 1175);

    const url = sheetCanvas.toDataURL('image/jpeg', 0.98);
    showSavedScreen(url, `Passport_4x6_Sheet_${Date.now()}.jpg`);
  });

  // ==========================================================================
  // 11. Saved & Share Screen
  // ==========================================================================
  function showSavedScreen(dataUrl, filename) {
    state.savedDataUrl = dataUrl;
    savedResultImg.src = dataUrl;
    savedFileName.textContent = filename;
    navigateTo('SAVED_SHARE');
  }

  savedShareBtn.addEventListener('click', () => {
    if (navigator.share && state.savedDataUrl) {
      fetch(state.savedDataUrl)
        .then(res => res.blob())
        .then(blob => {
          const file = new File([blob], savedFileName.textContent, { type: blob.type });
          navigator.share({ files: [file], title: 'Aura AI 4K Portrait', text: 'Enhanced with Aura AI ReminiSkinAI!' })
            .catch(() => triggerDirectDownload(state.savedDataUrl, savedFileName.textContent));
        })
        .catch(() => triggerDirectDownload(state.savedDataUrl, savedFileName.textContent));
    } else {
      triggerDirectDownload(state.savedDataUrl, savedFileName.textContent);
    }
  });

  savedAnotherBtn.addEventListener('click', () => navigateTo('HOME'));

  function triggerDirectDownload(url, filename) {
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  // ==========================================================================
  // 12. Modals Handling (Export 4K, Credits, Settings, Tone Quick Modal)
  // ==========================================================================
  // 4K Export Modal
  closeExportModalBtn.addEventListener('click', () => modalExport4k.classList.add('hidden'));

  document.querySelectorAll('.modal-radio-card').forEach(card => {
    card.addEventListener('click', () => {
      document.querySelectorAll('.modal-radio-card').forEach(c => c.classList.remove('active'));
      card.classList.add('active');
    });
  });

  document.querySelectorAll('.format-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.format-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
    });
  });

  modalConfirmSaveBtn.addEventListener('click', () => {
    modalExport4k.classList.add('hidden');
    const is4k = document.querySelector('input[name="modalResOption"]:checked').value === '4k';
    const isPng = document.querySelector('input[name="modalFmtOption"]:checked').value === 'png';

    const img = state.sourceImage;
    let targetW = img.naturalWidth, targetH = img.naturalHeight;
    if (is4k) {
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

    const processed = runSkinToneGrading(
      imgData, targetW, targetH,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity
    );
    ctx.putImageData(new ImageData(processed, targetW, targetH), 0, 0);

    const mime = isPng ? 'image/png' : 'image/jpeg';
    const ext = isPng ? 'png' : 'jpg';
    const url = expCanvas.toDataURL(mime, 0.98);
    const fname = `AuraAI_${preset.id}_4K_${Date.now()}.${ext}`;
    showSavedScreen(url, fname);
  });

  // Credits Modal
  creditsBtn.addEventListener('click', () => {
    creditsDialogBalance.textContent = state.userCredits;
    modalCredits.classList.remove('hidden');
  });
  closeCreditsModalBtn.addEventListener('click', () => modalCredits.classList.add('hidden'));
  claimBonusCreditsBtn.addEventListener('click', () => {
    state.userCredits += 10;
    creditsCountText.textContent = `${state.userCredits} Credits`;
    creditsDialogBalance.textContent = state.userCredits;
    modalCredits.classList.add('hidden');
  });

  // Settings Modal
  settingsBtn.addEventListener('click', () => modalSettings.classList.remove('hidden'));
  closeSettingsModalBtn.addEventListener('click', () => modalSettings.classList.add('hidden'));

  // Skin Tone Quick Modal
  studioSkinToneBtn.addEventListener('click', () => {
    populateModalTones();
    modalSkinTones.classList.remove('hidden');
  });
  closeSkinTonesModalBtn.addEventListener('click', () => modalSkinTones.classList.add('hidden'));

  function populateModalTones() {
    modalTonesGrid.innerHTML = '';
    AESTHETIC_PRESETS.forEach(p => {
      const card = document.createElement('div');
      card.className = `preset-card-aura ${p.id === state.selectedPresetId ? 'active' : ''}`;
      card.innerHTML = `
        <div class="preset-aura-header">
          <span class="preset-swatch-aura" style="background:${p.swatch};"></span>
          <span class="preset-aura-title">${p.title}</span>
        </div>
        <p class="preset-aura-sub">${p.subtitle}</p>
      `;
      card.addEventListener('click', () => {
        state.selectedPresetId = p.id;
        modalSkinTones.classList.add('hidden');
        renderStudioCanvas();
      });
      modalTonesGrid.appendChild(card);
    });
  }

  // ==========================================================================
  // 13. Home Actions & Photo Pickers
  // ==========================================================================
  let pendingTargetMode = 'COMPARISON_STUDIO';
  function triggerImageUpload(targetMode, presetId) {
    pendingTargetMode = targetMode;
    if (presetId) state.selectedPresetId = presetId;
    globalImageInput.click();
  }

  globalImageInput.addEventListener('change', (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (evt) => {
        startProcessingPipeline(evt.target.result, evt.target.result, pendingTargetMode);
      };
      reader.readAsDataURL(file);
    }
  });

  heroEnhanceBtn.addEventListener('click', () => {
    triggerImageUpload('COMPARISON_STUDIO', 'REMAINI_NATURAL');
  });

  // Precision Tools clicks
  document.querySelectorAll('.bento-card').forEach(card => {
    card.addEventListener('click', () => {
      const tool = card.getAttribute('data-tool');
      if (tool === 'portrait_4k') {
        triggerImageUpload('COMPARISON_STUDIO', 'REMINI_NATURAL');
      } else if (tool === 'old_restore') {
        triggerImageUpload('RETOUCH_SUITE', 'REMINI_NATURAL');
      } else if (tool === 'denoise') {
        triggerImageUpload('RETOUCH_SUITE', 'REMINI_NATURAL');
      } else if (tool === 'face_glow') {
        triggerImageUpload('RETOUCH_SUITE', 'KOREAN');
      }
    });
  });

  // Recent Restorations clicks
  document.querySelectorAll('.recent-card').forEach(card => {
    card.addEventListener('click', () => {
      const s = card.getAttribute('data-sample');
      if (s === '1') {
        startProcessingPipeline('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'COMPARISON_STUDIO', 'REMINI_NATURAL');
      } else if (s === '2') {
        startProcessingPipeline('assets/sample_2_before.webp', 'assets/sample_2_remini_ref.webp', 'COMPARISON_STUDIO', 'REMINI_NATURAL');
      } else {
        startProcessingPipeline('assets/sample_3_before.webp', 'assets/sample_3_before.webp', 'COMPARISON_STUDIO', 'MEDITERRANEAN');
      }
    });
  });

  viewAllToolsBtn.addEventListener('click', () => navigateTo('PASSPORT_STUDIO'));
  recentHistoryBtn.addEventListener('click', () => {
    startProcessingPipeline('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'COMPARISON_STUDIO');
  });

  // Initial load
  populateRetouchPresets();
  populatePassportSuits();
  loadImagesThenNavigate('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'HOME');

})();
