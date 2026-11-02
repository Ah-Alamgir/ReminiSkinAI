/**
 * Aura AI — ReminiSkinAI 4K Web App Core
 * Direct replication of Compose Multiplatform Native iOS UI & Core Math
 */

(() => {
  'use strict';

  // ==========================================================================
  // 1. Service Worker & PWA Setup
  // ==========================================================================
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js', { scope: './' })
        .then((reg) => console.log('[Aura PWA] Registered:', reg.scope))
        .catch((err) => console.warn('[Aura PWA] Registration error:', err));
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

  // ==========================================================================
  // 3. Application State (Matches Compose State)
  // ==========================================================================
  const state = {
    currentScreen: 'HOME', // HOME, COMPARISON_STUDIO, RETOUCH_SUITE, PROJECTS, PROCESSING, SAVED_SHARE
    targetScreenAfterProcessing: 'COMPARISON_STUDIO',
    userCredits: 48,

    // Image Buffers
    sourceImage: null,
    sourceBeforeSrc: 'assets/sample_1_before.webp',
    sourceAfterSrc: 'assets/sample_1_remini_ref.webp',
    sourceLoaded: false,
    savedDataUrl: null,

    // Retouch & Calibration Params
    selectedPresetId: 'REMINI_NATURAL',
    warmth: 1.0,
    radiance: 1.0,
    smoothness: 0.85,
    clarity: 0.82,

    // Studio Interactions
    splitPos: 50,
    zoomFactor: 1.0,
    isHoldActive: false,
    showProHud: true
  };

  // ==========================================================================
  // 4. DOM Elements
  // ==========================================================================
  // Views
  const viewHome = document.getElementById('viewHome');
  const viewComparison = document.getElementById('viewComparison');
  const viewRetouch = document.getElementById('viewRetouch');
  const viewProjects = document.getElementById('viewProjects');
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

  // Mode Chips in Studio
  const btnModeSplit = document.getElementById('btnModeSplit');
  const btnModeMagnifier = document.getElementById('btnModeMagnifier');
  const btnModePro = document.getElementById('btnModePro');
  const btnModeSkinToneQuick = document.getElementById('btnModeSkinToneQuick');
  const btnModeSwatchTiny = document.getElementById('btnModeSwatchTiny');
  const btnModeToneLabel = document.getElementById('btnModeToneLabel');

  // Retouch Elements
  const retouchBackBtn = document.getElementById('retouchBackBtn');
  const retouchStudioSplitBtn = document.getElementById('retouchStudioSplitBtn');
  const retouchSettingsBtn = document.getElementById('retouchSettingsBtn');
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

  // Projects Elements
  const projectsBackBtn = document.getElementById('projectsBackBtn');

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
  // 5. Screen Navigation Router (Matches Compose EditorScreenMode)
  // ==========================================================================
  function navigateTo(screen) {
    state.currentScreen = screen;
    const views = [viewHome, viewComparison, viewRetouch, viewProjects, viewProcessing, viewSaved];
    views.forEach(v => {
      if (v) v.classList.remove('active');
    });

    // Handle bottom navigation visibility (only visible on HOME)
    if (screen === 'HOME') {
      if (viewHome) viewHome.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'flex';
      updateNavHighlight('HOME');
    } else if (screen === 'COMPARISON_STUDIO') {
      if (viewComparison) viewComparison.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'none';
      renderStudioCanvas();
    } else if (screen === 'RETOUCH_SUITE') {
      if (viewRetouch) viewRetouch.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'none';
      renderRetouchCanvas();
    } else if (screen === 'PROJECTS') {
      if (viewProjects) viewProjects.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'none';
    } else if (screen === 'PROCESSING') {
      if (viewProcessing) viewProcessing.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'none';
    } else if (screen === 'SAVED_SHARE') {
      if (viewSaved) viewSaved.classList.add('active');
      if (mainBottomNav) mainBottomNav.style.display = 'none';
    }
  }

  function updateNavHighlight(navKey) {
    document.querySelectorAll('.nav-item').forEach(item => {
      item.classList.toggle('active', item.getAttribute('data-nav') === navKey);
    });
  }

  // Floating Glass Bottom Navigation Bar Clicks (MainScreen.kt lines 505-520)
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => {
      const navKey = item.getAttribute('data-nav');
      if (navKey === 'HOME') {
        navigateTo('HOME');
      } else if (navKey === 'STUDIO') {
        if (!state.sourceLoaded) {
          startProcessingPipeline('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'COMPARISON_STUDIO');
        } else {
          navigateTo('COMPARISON_STUDIO');
        }
      } else if (navKey === 'RESTORE' || navKey === 'RETOUCH') {
        triggerImageUpload('RETOUCH_SUITE', 'REMINI_NATURAL');
      } else if (navKey === 'PROJECTS') {
        navigateTo('PROJECTS');
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
        loadImagesThenNavigate(beforeSrc, afterSrc, targetMode);
        return;
      }
      const st = PIPELINE_STAGES[stageIdx];
      if (procStageStep) procStageStep.textContent = st.step;
      if (procStageTitle) procStageTitle.textContent = st.title;
      if (procStageDesc) procStageDesc.textContent = st.desc;
      if (procProgressFill) procProgressFill.style.width = `${st.pct}%`;
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
      const fallback = new Image();
      fallback.onload = () => {
        state.sourceImage = fallback;
        state.sourceLoaded = true;
        navigateTo(targetMode);
      };
      fallback.src = beforeSrc;
    };
    img.src = afterSrc || beforeSrc;
  }

  if (procCancelBtn) {
    procCancelBtn.addEventListener('click', () => {
      if (pipelineTimer) clearTimeout(pipelineTimer);
      navigateTo('HOME');
    });
  }

  // ==========================================================================
  // 7. Core SkinToneProcessor (Identical Math to Kotlin Multiplatform)
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
          const idx = row + x;
          const pIdx = idx * 4;
          for (let c = 0; c < 3; c++) {
            const center = outData[pIdx + c];
            const sumNeighbors = outData[(rowAbove + x) * 4 + c] +
                                 outData[(rowBelow + x) * 4 + c] +
                                 outData[(row + x - 1) * 4 + c] +
                                 outData[(row + x + 1) * 4 + c];
            const laplacian = center * 4 - sumNeighbors;
            enhanced[pIdx + c] = Math.min(255, Math.max(0, Math.round(center + laplacian * sharpenAmount * 0.15)));
          }
        }
      }
      return enhanced;
    }

    return outData;
  }

  // ==========================================================================
  // 8. Studio Rendering & Split Slider (BeforeAfterSlider.kt)
  // ==========================================================================
  function renderStudioCanvas() {
    if (!state.sourceImage || !state.sourceLoaded) return;
    const img = state.sourceImage;
    const maxW = 600;
    const scale = Math.min(1.0, maxW / img.naturalWidth);
    const w = Math.round(img.naturalWidth * scale);
    const h = Math.round(img.naturalHeight * scale);

    // Setup Canvas Before (Original / Unenhanced)
    canvasBefore.width = w;
    canvasBefore.height = h;
    const ctxBefore = canvasBefore.getContext('2d');
    const beforeImg = new Image();
    beforeImg.crossOrigin = 'anonymous';
    beforeImg.onload = () => {
      ctxBefore.drawImage(beforeImg, 0, 0, w, h);
    };
    beforeImg.src = state.sourceBeforeSrc;

    // Setup Canvas After (Enhanced + Graded)
    canvasAfter.width = w;
    canvasAfter.height = h;
    const ctxAfter = canvasAfter.getContext('2d');
    ctxAfter.drawImage(img, 0, 0, w, h);

    const imgData = ctxAfter.getImageData(0, 0, w, h);
    const preset = AESTHETIC_PRESETS.find(p => p.id === state.selectedPresetId) || AESTHETIC_PRESETS[0];

    const processed = runSkinToneGrading(
      imgData, w, h,
      preset,
      state.warmth,
      state.radiance,
      state.smoothness,
      state.clarity
    );
    ctxAfter.putImageData(new ImageData(processed, w, h), 0, 0);

    // Update HUD preset details
    if (hudSwatchMini) hudSwatchMini.style.background = preset.swatch;
    if (hudPresetTitle) hudPresetTitle.textContent = preset.title;
    if (btnModeSwatchTiny) btnModeSwatchTiny.style.background = preset.swatch;
    if (btnModeToneLabel) btnModeToneLabel.textContent = preset.title.split(' ')[0];

    updateSplitSliderPos(state.isHoldActive ? 100 : state.splitPos);
  }

  function updateSplitSliderPos(percent) {
    const clamped = Math.min(100, Math.max(0, percent));
    if (canvasBeforeWrapper) {
      canvasBeforeWrapper.style.width = `${clamped}%`;
    }
    if (studioDivider) {
      studioDivider.style.left = `${clamped}%`;
    }
  }

  // Interactive Split Slider Dragging
  let isDraggingSplit = false;
  function handleSplitPointer(e) {
    if (!studioCanvasContainer) return;
    const rect = studioCanvasContainer.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const x = clientX - rect.left;
    const pct = (x / rect.width) * 100;
    state.splitPos = Math.min(99, Math.max(1, pct));
    updateSplitSliderPos(state.splitPos);
  }

  if (studioDivider) {
    studioDivider.addEventListener('mousedown', () => { isDraggingSplit = true; });
    studioDivider.addEventListener('touchstart', () => { isDraggingSplit = true; }, { passive: true });
  }

  window.addEventListener('mousemove', (e) => {
    if (isDraggingSplit) handleSplitPointer(e);
  });
  window.addEventListener('mouseup', () => { isDraggingSplit = false; });
  window.addEventListener('touchmove', (e) => {
    if (isDraggingSplit) handleSplitPointer(e);
  }, { passive: true });
  window.addEventListener('touchend', () => { isDraggingSplit = false; });

  // Zoom Toggle (100% / 200%)
  if (studioZoomBtn) {
    studioZoomBtn.addEventListener('click', () => {
      state.zoomFactor = state.zoomFactor > 1.2 ? 1.0 : 1.9;
      if (hudZoomText) hudZoomText.textContent = state.zoomFactor > 1.2 ? '200%' : '100%';
      studioZoomBtn.style.background = state.zoomFactor > 1.2 ? 'rgba(139, 92, 246, 0.35)' : 'rgba(255, 255, 255, 0.08)';
      if (studioCanvasContainer) {
        studioCanvasContainer.style.transform = `scale(${state.zoomFactor})`;
        studioCanvasContainer.style.transition = 'transform 0.25s cubic-bezier(0.2, 0.8, 0.2, 1)';
      }
    });
  }

  // Hold to Compare
  function setHoldCompare(active) {
    state.isHoldActive = active;
    updateSplitSliderPos(active ? 100 : state.splitPos);
    if (studioHoldBtn) {
      studioHoldBtn.style.background = active ? 'var(--aura-secondary-container)' : 'rgba(255, 255, 255, 0.08)';
    }
  }
  if (studioHoldBtn) {
    studioHoldBtn.addEventListener('mousedown', () => setHoldCompare(true));
    studioHoldBtn.addEventListener('mouseup', () => setHoldCompare(false));
    studioHoldBtn.addEventListener('mouseleave', () => setHoldCompare(false));
    studioHoldBtn.addEventListener('touchstart', (e) => { e.preventDefault(); setHoldCompare(true); });
    studioHoldBtn.addEventListener('touchend', () => setHoldCompare(false));
  }

  // Exit Studio
  if (studioExitBtn) {
    studioExitBtn.addEventListener('click', () => navigateTo('HOME'));
  }

  // Retouch Shortcut
  if (studioRetouchShortcutBtn) {
    studioRetouchShortcutBtn.addEventListener('click', () => navigateTo('RETOUCH_SUITE'));
  }

  // Studio Intensity Slider
  if (dockIntensitySlider) {
    dockIntensitySlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      state.clarity = val / 100;
      if (dockIntensityVal) dockIntensityVal.textContent = `${val}%`;
      renderStudioCanvas();
    });
  }

  if (studioRegenerateBtn) {
    studioRegenerateBtn.addEventListener('click', () => {
      startProcessingPipeline(state.sourceBeforeSrc, state.sourceAfterSrc, 'COMPARISON_STUDIO');
    });
  }

  if (studioExportBtn) {
    studioExportBtn.addEventListener('click', () => {
      if (modalExport4k) modalExport4k.classList.remove('hidden');
    });
  }

  // Studio Mode Chips
  if (btnModeSplit) {
    btnModeSplit.addEventListener('click', () => {
      document.querySelectorAll('.mode-chip').forEach(c => c.classList.remove('active'));
      btnModeSplit.classList.add('active');
      state.splitPos = 50;
      updateSplitSliderPos(50);
    });
  }

  if (btnModeMagnifier) {
    btnModeMagnifier.addEventListener('click', () => {
      if (studioZoomBtn) studioZoomBtn.click();
    });
  }

  if (btnModePro) {
    btnModePro.addEventListener('click', () => {
      state.showProHud = !state.showProHud;
      btnModePro.classList.toggle('active', state.showProHud);
      const hudPill = document.querySelector('.studio-telemetry-chip');
      if (hudPill) hudPill.style.display = state.showProHud ? 'flex' : 'none';
    });
  }

  if (btnModeSkinToneQuick) {
    btnModeSkinToneQuick.addEventListener('click', () => {
      populateModalTones();
      if (modalSkinTones) modalSkinTones.classList.remove('hidden');
    });
  }

  // ==========================================================================
  // 9. Retouch Suite Rendering
  // ==========================================================================
  function renderRetouchCanvas() {
    if (!state.sourceImage || !state.sourceLoaded || !retouchCanvas) return;
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

  function populateRetouchPresets() {
    if (!retouchPresetsContainer) return;
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

  if (retouchBackBtn) {
    retouchBackBtn.addEventListener('click', () => navigateTo('HOME'));
  }
  if (retouchStudioSplitBtn) {
    retouchStudioSplitBtn.addEventListener('click', () => navigateTo('COMPARISON_STUDIO'));
  }
  if (retouchSettingsBtn) {
    retouchSettingsBtn.addEventListener('click', () => {
      if (modalSettings) modalSettings.classList.remove('hidden');
    });
  }

  // Retouch Sub-tabs
  document.querySelectorAll('.retouch-subtab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.retouch-subtab').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const tab = btn.getAttribute('data-subtab');
      if (tab === 'TONES') {
        if (subtabTonesContent) subtabTonesContent.style.display = 'flex';
        if (subtabCalibrationContent) subtabCalibrationContent.style.display = 'none';
      } else {
        if (subtabTonesContent) subtabTonesContent.style.display = 'none';
        if (subtabCalibrationContent) subtabCalibrationContent.style.display = 'flex';
      }
    });
  });

  // Retouch Calibration Sliders
  if (rSliderWarmth) {
    rSliderWarmth.addEventListener('input', (e) => {
      state.warmth = parseFloat(e.target.value);
      if (rValWarmth) rValWarmth.textContent = `${state.warmth.toFixed(2)}×`;
      renderRetouchCanvas();
    });
  }
  if (rSliderRadiance) {
    rSliderRadiance.addEventListener('input', (e) => {
      state.radiance = parseFloat(e.target.value);
      if (rValRadiance) rValRadiance.textContent = `${state.radiance.toFixed(2)}×`;
      renderRetouchCanvas();
    });
  }
  if (rSliderSmoothness) {
    rSliderSmoothness.addEventListener('input', (e) => {
      state.smoothness = parseFloat(e.target.value);
      if (rValSmoothness) rValSmoothness.textContent = `${Math.round(state.smoothness * 100)}%`;
      renderRetouchCanvas();
    });
  }
  if (rSliderClarity) {
    rSliderClarity.addEventListener('input', (e) => {
      state.clarity = parseFloat(e.target.value);
      if (rValClarity) rValClarity.textContent = `${Math.round(state.clarity * 100)}%`;
      renderRetouchCanvas();
    });
  }

  // ==========================================================================
  // 10. Projects View Actions
  // ==========================================================================
  if (projectsBackBtn) {
    projectsBackBtn.addEventListener('click', () => navigateTo('HOME'));
  }

  document.querySelectorAll('.project-item-card').forEach(card => {
    card.addEventListener('click', () => {
      const sample = card.getAttribute('data-sample');
      if (sample === '1') {
        startProcessingPipeline('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'COMPARISON_STUDIO', 'REMINI_NATURAL');
      } else if (sample === '2') {
        startProcessingPipeline('assets/sample_2_before.webp', 'assets/sample_2_remini_ref.webp', 'COMPARISON_STUDIO', 'REMINI_NATURAL');
      } else {
        startProcessingPipeline('assets/sample_3_before.webp', 'assets/sample_3_before.webp', 'COMPARISON_STUDIO', 'MEDITERRANEAN');
      }
    });
  });

  // ==========================================================================
  // 11. Saved & Share Screen
  // ==========================================================================
  function showSavedScreen(dataUrl, filename) {
    state.savedDataUrl = dataUrl;
    if (savedResultImg) savedResultImg.src = dataUrl;
    if (savedFileName) savedFileName.textContent = filename;
    navigateTo('SAVED_SHARE');
  }

  if (savedShareBtn) {
    savedShareBtn.addEventListener('click', () => {
      if (navigator.share && state.savedDataUrl) {
        fetch(state.savedDataUrl)
          .then(res => res.blob())
          .then(blob => {
            const file = new File([blob], savedFileName.textContent, { type: blob.type });
            navigator.share({
              files: [file],
              title: 'Aura AI Ultra 4K Photo',
              text: 'Enhanced with ReminiSkinAI 4K Luminescent Glass Engine'
            });
          })
          .catch(() => triggerDirectDownload(state.savedDataUrl, savedFileName.textContent));
      } else {
        triggerDirectDownload(state.savedDataUrl, savedFileName.textContent);
      }
    });
  }

  if (savedAnotherBtn) {
    savedAnotherBtn.addEventListener('click', () => navigateTo('HOME'));
  }

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
  if (closeExportModalBtn) {
    closeExportModalBtn.addEventListener('click', () => {
      if (modalExport4k) modalExport4k.classList.add('hidden');
    });
  }

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

  if (modalConfirmSaveBtn) {
    modalConfirmSaveBtn.addEventListener('click', () => {
      if (modalExport4k) modalExport4k.classList.add('hidden');
      const is4kEl = document.querySelector('input[name="modalResOption"]:checked');
      const isPngEl = document.querySelector('input[name="modalFmtOption"]:checked');
      const is4k = is4kEl ? is4kEl.value === '4k' : true;
      const isPng = isPngEl ? isPngEl.value === 'png' : false;

      const img = state.sourceImage;
      if (!img) return;

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
  }

  // Credits Modal
  if (creditsBtn) {
    creditsBtn.addEventListener('click', () => {
      if (creditsDialogBalance) creditsDialogBalance.textContent = state.userCredits;
      if (modalCredits) modalCredits.classList.remove('hidden');
    });
  }
  if (closeCreditsModalBtn) {
    closeCreditsModalBtn.addEventListener('click', () => {
      if (modalCredits) modalCredits.classList.add('hidden');
    });
  }
  if (claimBonusCreditsBtn) {
    claimBonusCreditsBtn.addEventListener('click', () => {
      state.userCredits += 10;
      if (creditsCountText) creditsCountText.textContent = `${state.userCredits} Credits`;
      if (creditsDialogBalance) creditsDialogBalance.textContent = state.userCredits;
      if (modalCredits) modalCredits.classList.add('hidden');
    });
  }

  // Settings Modal
  if (settingsBtn) {
    settingsBtn.addEventListener('click', () => {
      if (modalSettings) modalSettings.classList.remove('hidden');
    });
  }
  if (closeSettingsModalBtn) {
    closeSettingsModalBtn.addEventListener('click', () => {
      if (modalSettings) modalSettings.classList.add('hidden');
    });
  }

  // Skin Tone Quick Modal
  if (studioSkinToneBtn) {
    studioSkinToneBtn.addEventListener('click', () => {
      populateModalTones();
      if (modalSkinTones) modalSkinTones.classList.remove('hidden');
    });
  }
  if (closeSkinTonesModalBtn) {
    closeSkinTonesModalBtn.addEventListener('click', () => {
      if (modalSkinTones) modalSkinTones.classList.add('hidden');
    });
  }

  function populateModalTones() {
    if (!modalTonesGrid) return;
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
        if (modalSkinTones) modalSkinTones.classList.add('hidden');
        renderStudioCanvas();
      });
      modalTonesGrid.appendChild(card);
    });
  }

  // ==========================================================================
  // 13. Home Actions & Native Photo Picker
  // ==========================================================================
  let pendingTargetMode = 'COMPARISON_STUDIO';
  function triggerImageUpload(targetMode, presetId) {
    pendingTargetMode = targetMode;
    if (presetId) state.selectedPresetId = presetId;
    if (globalImageInput) globalImageInput.click();
  }

  if (globalImageInput) {
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
  }

  if (heroEnhanceBtn) {
    heroEnhanceBtn.addEventListener('click', () => {
      triggerImageUpload('COMPARISON_STUDIO', 'REMINI_NATURAL');
    });
  }

  // Precision Tools clicks (MainScreen.kt lines 450-465)
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

  // Recent Restorations clicks (MainScreen.kt lines 472-490)
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

  if (viewAllToolsBtn) {
    viewAllToolsBtn.addEventListener('click', () => navigateTo('PROJECTS'));
  }
  if (recentHistoryBtn) {
    recentHistoryBtn.addEventListener('click', () => navigateTo('PROJECTS'));
  }

  // Initial load
  populateRetouchPresets();
  loadImagesThenNavigate('assets/sample_1_before.webp', 'assets/sample_1_remini_ref.webp', 'HOME');

})();
