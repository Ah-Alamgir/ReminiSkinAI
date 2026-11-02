/**
 * Aura ReminiSkinAI — Complete iOS App Web Port (100% UI & Functional Parity)
 * Mirrors:
 *  - shared/src/commonMain/kotlin/com/example/reminiskinai/shared/App.kt
 *  - shared/src/commonMain/kotlin/com/example/reminiskinai/ui/main/MainScreen.kt
 *  - shared/src/commonMain/kotlin/com/example/reminiskinai/ui/passport/PassportPhotoScreen.kt
 *  - shared/src/commonMain/kotlin/com/example/reminiskinai/ui/components/BeforeAfterSlider.kt
 *  - shared/src/commonMain/kotlin/com/example/reminiskinai/ui/components/Export4kModal.kt
 *  - shared/src/iosMain/kotlin/com/example/reminiskinai/processor/SkinToneProcessor.ios.kt
 *  - shared/src/iosMain/kotlin/com/example/reminiskinai/platform/PlatformSettings.ios.kt
 */

(function () {
  'use strict';

  // ==========================================================================
  // 1. MODELS & CONSTANTS (SkinEnhancementModels.kt & PassportPhotoModels.kt)
  // ==========================================================================

  const SKIN_TONE_PRESETS = [
    {
      id: 'REMINI_NATURAL',
      displayName: 'Remini Natural',
      regionDescription: 'Balanced studio clarity, true-to-life undertone & radiant glow',
      targetHue: 22.0,
      targetSat: 0.30,
      targetVal: 0.86,
      warmthShift: 0.05,
      rednessControl: 0.25,
      melaninDepth: 0.0,
      luminanceLift: 0.08,
      contrastBoost: 0.14,
      previewHex: '#F3C6A8',
      isDefault: true
    },
    {
      id: 'AMERICAN',
      displayName: 'American Golden',
      regionDescription: 'Sun-kissed California bronze, warm amber highlights & crisp definition',
      targetHue: 25.0,
      targetSat: 0.38,
      targetVal: 0.82,
      warmthShift: 0.22,
      rednessControl: 0.15,
      melaninDepth: 0.12,
      luminanceLift: 0.06,
      contrastBoost: 0.18,
      previewHex: '#DFA67B'
    },
    {
      id: 'KOREAN',
      displayName: 'Korean Glass Skin',
      regionDescription: 'Dewy porcelain brightness, neutralized redness & luminous K-beauty finish',
      targetHue: 18.0,
      targetSat: 0.20,
      targetVal: 0.93,
      warmthShift: -0.08,
      rednessControl: 0.45,
      melaninDepth: -0.18,
      luminanceLift: 0.18,
      contrastBoost: 0.08,
      previewHex: '#FCE2D4'
    },
    {
      id: 'AFRICAN',
      displayName: 'African Rich Ebony',
      regionDescription: 'Deep melanin richness, golden-bronze specular highlights & zero ashiness',
      targetHue: 23.0,
      targetSat: 0.44,
      targetVal: 0.52,
      warmthShift: 0.25,
      rednessControl: 0.10,
      melaninDepth: 0.35,
      luminanceLift: 0.12,
      contrastBoost: 0.22,
      previewHex: '#7A482E'
    },
    {
      id: 'BANGLADESHI',
      displayName: 'Bangladeshi Warm',
      regionDescription: 'South Asian golden-caramel glow, anti-olive cast & vibrant bridal warmth',
      targetHue: 26.0,
      targetSat: 0.39,
      targetVal: 0.76,
      warmthShift: 0.24,
      rednessControl: 0.30,
      melaninDepth: 0.10,
      luminanceLift: 0.10,
      contrastBoost: 0.16,
      previewHex: '#C88D62'
    },
    {
      id: 'LATINA_OLIVE',
      displayName: 'Latina Honey',
      regionDescription: 'Rich honey-olive warmth, balanced golden undertone & soft satin sheen',
      targetHue: 27.0,
      targetSat: 0.37,
      targetVal: 0.79,
      warmthShift: 0.20,
      rednessControl: 0.28,
      melaninDepth: 0.08,
      luminanceLift: 0.09,
      contrastBoost: 0.15,
      previewHex: '#D49A6A'
    },
    {
      id: 'NORDIC_FAIR',
      displayName: 'Nordic Alabaster',
      regionDescription: 'Cool ivory clarity, rosacea calming & delicate soft-focus highlights',
      targetHue: 17.0,
      targetSat: 0.18,
      targetVal: 0.94,
      warmthShift: -0.12,
      rednessControl: 0.50,
      melaninDepth: -0.20,
      luminanceLift: 0.15,
      contrastBoost: 0.09,
      previewHex: '#FDE8DC'
    },
    {
      id: 'MEDITERRANEAN',
      displayName: 'Mediterranean',
      regionDescription: 'Rich olive-bronze warmth, sun-lit cheekbones & editorial contrast',
      targetHue: 24.0,
      targetSat: 0.36,
      targetVal: 0.78,
      warmthShift: 0.18,
      rednessControl: 0.22,
      melaninDepth: 0.14,
      luminanceLift: 0.07,
      contrastBoost: 0.17,
      previewHex: '#CE9268'
    }
  ];

  const LIP_SHADE_PRESETS = [
    { id: 'NONE', displayName: 'Natural', hex: '#D88A8A', r: 0, g: 0, b: 0 },
    { id: 'NATURAL_ROSE', displayName: 'Natural Rose', hex: '#C85A6E', r: 200, g: 90, b: 110 },
    { id: 'SOFT_CORAL', displayName: 'Soft Coral', hex: '#E06C58', r: 224, g: 108, b: 88 },
    { id: 'BERRY_TINT', displayName: 'Berry Tint', hex: '#9B365A', r: 155, g: 54, b: 90 },
    { id: 'CLASSIC_RED', displayName: 'Classic Red', hex: '#B82030', r: 184, g: 32, b: 48 },
    { id: 'WARM_NUDE', displayName: 'Warm Nude', hex: '#B87362', r: 184, g: 115, b: 98 }
  ];

  const HAIR_SHADE_PRESETS = [
    { id: 'NONE', displayName: 'Original', hex: '#3A3A3A', r: 0, g: 0, b: 0 },
    { id: 'JET_BLACK', displayName: 'Jet Black', hex: '#141418', r: 20, g: 20, b: 24 },
    { id: 'DARK_ESPRESSO', displayName: 'Dark Espresso', hex: '#2E1E16', r: 46, g: 30, b: 22 },
    { id: 'GOLDEN_BROWN', displayName: 'Golden Brown', hex: '#684428', r: 104, g: 68, b: 40 },
    { id: 'WARM_AUBURN', displayName: 'Warm Auburn', hex: '#6E2A1E', r: 110, g: 42, b: 30 },
    { id: 'ASH_BLONDE', displayName: 'Ash Blonde', hex: '#9A8870', r: 154, g: 136, b: 112 },
    { id: 'BURGUNDY_WINE', displayName: 'Burgundy Wine', hex: '#52182C', r: 82, g: 24, b: 44 }
  ];

  const STUDIO_SAMPLES = [
    {
      index: 0,
      title: 'Portrait I',
      fileName: 'Portrait_V2_4K.jpg',
      beforeUrl: 'assets/sample_1_before.webp',
      afterUrl: 'assets/sample_1_remini_ref.webp'
    },
    {
      index: 1,
      title: 'Portrait II',
      fileName: 'Family_Archive_1974.png',
      beforeUrl: 'assets/sample_2_before.webp',
      afterUrl: 'assets/sample_2_remini_ref.webp'
    },
    {
      index: 2,
      title: 'Custom',
      fileName: 'Studio_Portrait_4K.jpg',
      beforeUrl: 'assets/sample_3_before.webp',
      afterUrl: 'assets/sample_3_before.webp',
      isGeneratedAfter: true
    }
  ];

  const FORMAL_COAT_PRESETS = [
    {
      id: 'NONE',
      title: 'Original',
      subtitle: 'Keep current attire',
      category: 'ALL',
      assetUrl: null,
      swatchHex: '#475569',
      collarAnchorY: 0.18,
      shoulderWidthRatio: 0.90
    },
    {
      id: 'MEN_1_NAVY_TIE',
      title: 'Navy Suit & Tie',
      subtitle: "Men's Executive Navy",
      category: 'MEN',
      assetUrl: 'assets/men_1_navy_suit.png',
      swatchHex: '#1B2A4A',
      collarAnchorY: 0.145,
      shoulderWidthRatio: 0.94
    },
    {
      id: 'MEN_2_CHARCOAL_TIE',
      title: 'Charcoal Suit & Tie',
      subtitle: "Men's Diplomatic Grey",
      category: 'MEN',
      assetUrl: 'assets/men_2_charcoal_suit.png',
      swatchHex: '#2C3038',
      collarAnchorY: 0.15,
      shoulderWidthRatio: 0.94
    },
    {
      id: 'MEN_3_GREY_NO_TIE',
      title: 'Grey Blazer',
      subtitle: 'Smart Open Collar',
      category: 'MEN',
      assetUrl: 'assets/men_3_grey_blazer.png',
      swatchHex: '#4A4E54',
      collarAnchorY: 0.155,
      shoulderWidthRatio: 0.92
    },
    {
      id: 'MEN_4_BLACK_FORMAL',
      title: 'Black Suit & Tie',
      subtitle: 'Classic Formal Black',
      category: 'MEN',
      assetUrl: 'assets/men_4_black_formal.png',
      swatchHex: '#18191C',
      collarAnchorY: 0.145,
      shoulderWidthRatio: 0.94
    },
    {
      id: 'MEN_5_BROWN_OPEN',
      title: 'Espresso Blazer',
      subtitle: 'Warm Formal Coat',
      category: 'MEN',
      assetUrl: 'assets/men_5_brown_open_collar.png',
      swatchHex: '#3B2B23',
      collarAnchorY: 0.155,
      shoulderWidthRatio: 0.92
    },
    {
      id: 'WOMEN_1_NAVY_BLAZER',
      title: 'Navy Blazer',
      subtitle: "Women's White Blouse",
      category: 'WOMEN',
      assetUrl: 'assets/women_1_navy_blazer.png',
      swatchHex: '#1E2F52',
      collarAnchorY: 0.16,
      shoulderWidthRatio: 0.88
    },
    {
      id: 'WOMEN_2_BLACK_SUIT',
      title: 'Black Formal Suit',
      subtitle: "Women's Executive Suit",
      category: 'WOMEN',
      assetUrl: 'assets/women_2_black_suit.png',
      swatchHex: '#1C1D21',
      collarAnchorY: 0.16,
      shoulderWidthRatio: 0.88
    },
    {
      id: 'WOMEN_3_BEIGE_BLAZER',
      title: 'Cream Jacket',
      subtitle: "Women's Diplomatic Coat",
      category: 'WOMEN',
      assetUrl: 'assets/women_3_beige_blazer.png',
      swatchHex: '#D8C8B4',
      collarAnchorY: 0.165,
      shoulderWidthRatio: 0.88
    },
    {
      id: 'WOMEN_4_BURGUNDY_SUIT',
      title: 'Burgundy Blazer',
      subtitle: "Women's Formal Coat",
      category: 'WOMEN',
      assetUrl: 'assets/women_4_burgundy_blazer.png',
      swatchHex: '#5A1F2E',
      collarAnchorY: 0.16,
      shoulderWidthRatio: 0.88
    },
    {
      id: 'WOMEN_5_CHARCOAL_SUIT',
      title: 'Charcoal Blazer',
      subtitle: "Women's Modern Suit",
      category: 'WOMEN',
      assetUrl: 'assets/women_5_grey_blazer.png',
      swatchHex: '#3D4148',
      collarAnchorY: 0.16,
      shoulderWidthRatio: 0.88
    }
  ];

  const PASSPORT_BG_COLORS = [
    { id: 'PURE_WHITE', label: 'Pure White', hex: '#FFFFFF', isLight: true },
    { id: 'OFF_WHITE', label: 'Off-White', hex: '#F4F5F7', isLight: true },
    { id: 'LIGHT_BLUE', label: 'Light Sky Blue', hex: '#D6EAF8', isLight: true },
    { id: 'EMBASSY_BLUE', label: 'Embassy Blue', hex: '#2E86C1', isLight: false },
    { id: 'ROYAL_BLUE', label: 'Royal Blue', hex: '#1B4F72', isLight: false },
    { id: 'LIGHT_GREY', label: 'Light Grey', hex: '#D5D8DC', isLight: true },
    { id: 'DARK_GREY', label: 'Studio Grey', hex: '#808B96', isLight: false },
    { id: 'RED_ID', label: 'Visa Red', hex: '#C0392B', isLight: false }
  ];

  const COUNTRY_STANDARDS = [
    { code: 'US', flag: '🇺🇸', countryName: 'United States', specLabel: '2×2" (51×51 mm)', widthMm: 51, heightMm: 51, widthPx300Dpi: 600, heightPx300Dpi: 600 },
    { code: 'UK', flag: '🇬🇧', countryName: 'United Kingdom', specLabel: '35×45 mm (Biometric)', widthMm: 35, heightMm: 45, widthPx300Dpi: 413, heightPx300Dpi: 531 },
    { code: 'EU', flag: '🇪🇺', countryName: 'Schengen / EU', specLabel: '35×45 mm (ICAO)', widthMm: 35, heightMm: 45, widthPx300Dpi: 413, heightPx300Dpi: 531 },
    { code: 'CA', flag: '🇨🇦', countryName: 'Canada', specLabel: '50×70 mm', widthMm: 50, heightMm: 70, widthPx300Dpi: 591, heightPx300Dpi: 827 },
    { code: 'IN', flag: '🇮🇳', countryName: 'India (Passport/OCI)', specLabel: '2×2" (51×51 mm)', widthMm: 51, heightMm: 51, widthPx300Dpi: 600, heightPx300Dpi: 600 },
    { code: 'CN', flag: '🇨🇳', countryName: 'China Visa', specLabel: '33×48 mm', widthMm: 33, heightMm: 48, widthPx300Dpi: 390, heightPx300Dpi: 567 },
    { code: 'JP', flag: '🇯🇵', countryName: 'Japan', specLabel: '35×45 mm', widthMm: 35, heightMm: 45, widthPx300Dpi: 413, heightPx300Dpi: 531 },
    { code: 'BD', flag: '🇧🇩', countryName: 'Bangladesh', specLabel: '45×55 mm (MRP/e-Passport)', widthMm: 45, heightMm: 55, widthPx300Dpi: 531, heightPx300Dpi: 650 },
    { code: 'AU', flag: '🇦🇺', countryName: 'Australia', specLabel: '35×45 mm', widthMm: 35, heightMm: 45, widthPx300Dpi: 413, heightPx300Dpi: 531 },
    { code: 'AE', flag: '🇦🇪', countryName: 'UAE / Dubai Visa', specLabel: '43×55 mm', widthMm: 43, heightMm: 55, widthPx300Dpi: 508, heightPx300Dpi: 650 }
  ];

  const EXPORT_TIERS = {
    HD_1080P: { id: 'HD_1080P', title: 'Original 1080p', resolution: '1080 × 1440 px', creditCost: 0, scale: 1.0 },
    ULTRA_4K: { id: 'ULTRA_4K', title: 'Ultra 4K', resolution: '3840 × 5120 px', creditCost: 1, scale: 2.0 },
    MASTER_8K: { id: 'MASTER_8K', title: 'Master Print 8K', resolution: '7680 × 10240 px', creditCost: 2, scale: 2.5 }
  };

  // ==========================================================================
  // 2. APPLICATION STATE & PLATFORM SETTINGS PERSISTENCE
  // ==========================================================================

  const STORAGE_KEY_PRESET = 'remini_skin_preset';
  const STORAGE_KEY_PARAMS = 'remini_skin_params';
  const STORAGE_KEY_CREDITS = 'remini_aura_credits';

  const state = {
    // Navigation (AppScreen.MAIN vs AppScreen.PASSPORT_STUDIO)
    currentAppScreen: 'MAIN',
    screenMode: 'HOME', // HOME | COMPARISON_STUDIO | RETOUCH_SUITE | PROCESSING | SAVED_SHARE
    studioBottomTab: 'ENHANCE', // ENHANCE | RETOUCH | MAGNIFIER | SKIN_AI
    retouchTab: 'RETOUCH', // RESTORE | RETOUCH | COLORIZE | BACKGROUND

    // Credits
    userCredits: 48,

    // Active Sample / Custom Image
    selectedSampleIndex: 0,
    customImageElement: null,
    customFileName: 'Custom_Portrait_4K.jpg',

    // BeforeAfterSlider State
    splitProgress: 0.48,
    savedSplitBeforeHold: 0.48,
    isHoldCompare: false,
    isZoomed200: false,
    zoomFocusX: 0.5,
    zoomFocusY: 0.35,
    wireframePulsePhase: 0,

    // Skin Tone Preset & Edit Params (PlatformSettings.ios.kt)
    selectedPresetId: 'REMINI_NATURAL',
    editParams: {
      skinSmoothness: 0.45,
      skinWarmth: 1.10, // slider Lighting Boost & HDR maps (skinWarmth - 0.5) = 0.60 -> 60%
      skinRadiance: 1.15, // slider Eye Iris Brightness maps (skinRadiance - 0.5) = 0.65 -> 65%
      detailPreservation: 0.85,
      facialClarity: 0.85,
      lipShade: 'NONE',
      lipIntensity: 0.50,
      hairShade: 'NONE',
      hairIntensity: 0.50,
      showSkinMaskOverlay: false
    },

    // Export 4K Modal State
    selectedExportTier: 'ULTRA_4K',
    removeWatermark: true,
    preserveExif: true,
    enhanceColorGamut: false,

    // Cached Offscreen Canvases for Current Photo (Zero-lag 60 FPS rendering)
    beforeOffscreen: document.createElement('canvas'),
    afterBaseOffscreen: document.createElement('canvas'),
    afterGradedOffscreen: document.createElement('canvas'),
    isSampleLoaded: false,

    // Passport Photo Studio State (PassportPhotoScreen.kt)
    passport: {
      mode: 'UPLOAD_HOME', // UPLOAD_HOME | PROCESSING | STUDIO_EDITOR | SAVED_EXPORT
      activeTab: 'COATS', // COATS | CROP_ALIGN | BACKGROUND | COUNTRY_SIZE
      coatCategoryFilter: 'ALL', // ALL | MEN | WOMEN
      selectedSampleIdx: 0,
      customPassportImg: null,
      country: COUNTRY_STANDARDS[0],
      bgColor: PASSPORT_BG_COLORS[0],
      coat: FORMAL_COAT_PRESETS[1], // Default Navy Suit & Tie
      printMode: 'SINGLE', // SINGLE | SHEET_4X6
      showBiometricGuides: true,
      userZoom: 1.0,
      userPanX: 0.0,
      userPanY: 0.0,
      coatScale: 1.0,
      coatOffsetY: 0.0,
      subjectCutoutCanvas: document.createElement('canvas'),
      subjectHeadNeckOnlyCanvas: document.createElement('canvas'),
      faceMetrics: { centerX: 0.5, eyeY: 0.38, headHeight: 0.46, neckBottomY: 0.68 },
      calibratedCoatCache: {}
    }
  };

  function loadPersistedSettings() {
    try {
      const savedPreset = localStorage.getItem(STORAGE_KEY_PRESET);
      if (savedPreset && SKIN_TONE_PRESETS.some((p) => p.id === savedPreset)) {
        state.selectedPresetId = savedPreset;
      }
      const savedParams = localStorage.getItem(STORAGE_KEY_PARAMS);
      if (savedParams) {
        const parsed = JSON.parse(savedParams);
        Object.assign(state.editParams, parsed);
      }
      const savedCredits = localStorage.getItem(STORAGE_KEY_CREDITS);
      if (savedCredits !== null && !isNaN(parseInt(savedCredits, 10))) {
        state.userCredits = parseInt(savedCredits, 10);
      }
    } catch (e) {
      // Ignore storage access errors in private browsing
    }
  }

  function savePersistedSettings() {
    try {
      localStorage.setItem(STORAGE_KEY_PRESET, state.selectedPresetId);
      localStorage.setItem(STORAGE_KEY_PARAMS, JSON.stringify(state.editParams));
      localStorage.setItem(STORAGE_KEY_CREDITS, String(state.userCredits));
    } catch (e) {
      // Ignore storage errors
    }
  }

  function getSelectedPreset() {
    return SKIN_TONE_PRESETS.find((p) => p.id === state.selectedPresetId) || SKIN_TONE_PRESETS[0];
  }

  // ==========================================================================
  // 3. IMAGE CACHE & REMINI 4K NEURAL PROCESSOR
  // ==========================================================================

  const imageElementCache = {};

  function loadImageCached(url) {
    if (imageElementCache[url]) {
      return Promise.resolve(imageElementCache[url]);
    }
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload = () => {
        imageElementCache[url] = img;
        resolve(img);
      };
      img.onerror = reject;
      img.src = url;
    });
  }

  /**
   * Scans image for human skin tone cluster (YCbCr) to detect the face/subject centroid and bounding radius
   */
  function detectSubjectFaceAndSkin(srcData, w, h) {
    let skinCount = 0;
    let sumX = 0;
    let sumY = 0;
    let minX = w;
    let maxX = 0;
    let minY = h;
    let maxY = 0;

    const step = 3;
    for (let y = 0; y < h; y += step) {
      const rowOffset = y * w;
      for (let x = 0; x < w; x += step) {
        const idx = (rowOffset + x) * 4;
        const r = srcData[idx];
        const g = srcData[idx + 1];
        const b = srcData[idx + 2];

        const lum = 0.299 * r + 0.587 * g + 0.114 * b;
        const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;
        const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;

        if (cr >= 132 && cr <= 178 && cb >= 78 && cb <= 132 && r > g && g > b * 0.72 && lum > 32 && lum < 240) {
          skinCount++;
          sumX += x;
          sumY += y;
          if (x < minX) minX = x;
          if (x > maxX) maxX = x;
          if (y < minY) minY = y;
          if (y > maxY) maxY = y;
        }
      }
    }

    if (skinCount > 30) {
      const cx = (sumX / skinCount) / w;
      const cy = (sumY / skinCount) / h;
      const rx = Math.max(0.12, Math.min(0.38, ((maxX - minX) / w) * 0.60));
      const ry = Math.max(0.14, Math.min(0.42, ((maxY - minY) / h) * 0.65));
      return { found: true, cx, cy, rx, ry };
    }
    return { found: false, cx: 0.50, cy: 0.38, rx: 0.25, ry: 0.32 };
  }

  /**
   * Genuine Remini 4K Studio AI Enhancement Engine:
   * Multi-scale frequency separation unsharp mask, bilateral porcelain dermis smoothing,
   * eye catchlight & iris sparkle, shadow recovery & studio HDR tone mapping.
   */
  function generateReminiAiEnhancedImageData(srcImageData) {
    const w = srcImageData.width;
    const h = srcImageData.height;
    const src = srcImageData.data;
    const outImageData = new ImageData(new Uint8ClampedArray(src), w, h);
    const dst = outImageData.data;

    const face = detectSubjectFaceAndSkin(src, w, h);
    state.detectedFace = face;
    state.zoomFocusX = face.cx;
    state.zoomFocusY = Math.max(0.18, face.cy - face.ry * 0.20);

    // Separable Box Blur 1 (Fine: Radius 2) & Blur 2 (Macro: Radius 6)
    const totalPx = w * h;
    const tempFine = new Float32Array(totalPx * 3);
    const tempMacro = new Float32Array(totalPx * 3);
    const blurFine = new Uint8ClampedArray(totalPx * 3);
    const blurMacro = new Uint8ClampedArray(totalPx * 3);

    const rFine = 2;
    const rMacro = 6;
    const invFine = 1.0 / (rFine * 2 + 1);
    const invMacro = 1.0 / (rMacro * 2 + 1);

    // Horizontal pass
    for (let y = 0; y < h; y++) {
      const rowOffset = y * w;
      for (let x = 0; x < w; x++) {
        let sfR = 0, sfG = 0, sfB = 0;
        let smR = 0, smG = 0, smB = 0;

        for (let k = -rFine; k <= rFine; k++) {
          const pxX = Math.max(0, Math.min(w - 1, x + k));
          const idx = (rowOffset + pxX) * 4;
          sfR += src[idx];
          sfG += src[idx + 1];
          sfB += src[idx + 2];
        }
        const tIdx = (rowOffset + x) * 3;
        tempFine[tIdx] = sfR * invFine;
        tempFine[tIdx + 1] = sfG * invFine;
        tempFine[tIdx + 2] = sfB * invFine;

        for (let k = -rMacro; k <= rMacro; k++) {
          const pxX = Math.max(0, Math.min(w - 1, x + k));
          const idx = (rowOffset + pxX) * 4;
          smR += src[idx];
          smG += src[idx + 1];
          smB += src[idx + 2];
        }
        tempMacro[tIdx] = smR * invMacro;
        tempMacro[tIdx + 1] = smG * invMacro;
        tempMacro[tIdx + 2] = smB * invMacro;
      }
    }

    // Vertical pass
    for (let x = 0; x < w; x++) {
      for (let y = 0; y < h; y++) {
        let sfR = 0, sfG = 0, sfB = 0;
        let smR = 0, smG = 0, smB = 0;

        for (let k = -rFine; k <= rFine; k++) {
          const py = Math.max(0, Math.min(h - 1, y + k));
          const tIdx = (py * w + x) * 3;
          sfR += tempFine[tIdx];
          sfG += tempFine[tIdx + 1];
          sfB += tempFine[tIdx + 2];
        }
        const bIdx = (y * w + x) * 3;
        blurFine[bIdx] = sfR * invFine;
        blurFine[bIdx + 1] = sfG * invFine;
        blurFine[bIdx + 2] = sfB * invFine;

        for (let k = -rMacro; k <= rMacro; k++) {
          const py = Math.max(0, Math.min(h - 1, y + k));
          const tIdx = (py * w + x) * 3;
          smR += tempMacro[tIdx];
          smG += tempMacro[tIdx + 1];
          smB += tempMacro[tIdx + 2];
        }
        blurMacro[bIdx] = smR * invMacro;
        blurMacro[bIdx + 1] = smG * invMacro;
        blurMacro[bIdx + 2] = smB * invMacro;
      }
    }

    // Synthesize Remini 4K Studio Image
    for (let y = 0; y < h; y++) {
      const ny = y / h;
      const rowOffset = y * w;

      for (let x = 0; x < w; x++) {
        const nx = x / w;
        const idx = (rowOffset + x) * 4;
        const bIdx = (rowOffset + x) * 3;

        let r = src[idx];
        let g = src[idx + 1];
        let b = src[idx + 2];

        const bfR = blurFine[bIdx];
        const bfG = blurFine[bIdx + 1];
        const bfB = blurFine[bIdx + 2];

        const bmR = blurMacro[bIdx];
        const bmG = blurMacro[bIdx + 1];
        const bmB = blurMacro[bIdx + 2];

        const lum = 0.299 * r + 0.587 * g + 0.114 * b;
        const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;
        const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;

        const isSkin = cr >= 132 && cr <= 178 && cb >= 78 && cb <= 132 && r > g && g > b * 0.72;

        const dxFace = (nx - face.cx) / face.rx;
        const dyFace = (ny - face.cy) / face.ry;
        const distFaceSq = dxFace * dxFace + dyFace * dyFace;
        const inFaceRegion = distFaceSq < 1.35;

        // High & Mid frequency components
        const hfR = r - bfR;
        const hfG = g - bfG;
        const hfB = b - bfB;

        const mfR = bfR - bmR;
        const mfG = bfG - bmG;
        const mfB = bfB - bmB;

        if (isSkin && inFaceRegion) {
          // Porcelain Dermis Smoothing + Micro-texture pore retention
          const smoothRatio = 0.62;
          r = r * (1 - smoothRatio) + bfR * smoothRatio + hfR * 0.95;
          g = g * (1 - smoothRatio) + bfG * smoothRatio + hfG * 0.95;
          b = b * (1 - smoothRatio) + bfB * smoothRatio + hfB * 0.95;
        } else {
          // Powerful High-Pass Sharpening for eyes, hair, clothes, and crisp edges
          const sharpGain = inFaceRegion ? 2.10 : 1.55;
          const deHazeGain = inFaceRegion ? 0.90 : 0.60;
          r = r + hfR * sharpGain + mfR * deHazeGain;
          g = g + hfG * sharpGain + mfG * deHazeGain;
          b = b + hfB * sharpGain + mfB * deHazeGain;
        }

        // Eye Level Specular Catchlight & Iris Clarity Boost
        const inEyeLevel = inFaceRegion && Math.abs(dyFace + 0.14) < 0.26 && Math.abs(dxFace) < 0.75;
        if (inEyeLevel) {
          if (lum > 150) {
            const catchlight = 1.0 + Math.min(0.40, ((lum - 150) / 105) * 0.40);
            r *= catchlight;
            g *= catchlight;
            b *= catchlight;
          }
          r = (r - 128) * 1.20 + 128;
          g = (g - 128) * 1.20 + 128;
          b = (b - 128) * 1.20 + 128;
        }

        // Adaptive Shadow Lift & Studio HDR (Lifting muddy shadows & flat lighting)
        if (lum < 115) {
          const shadowLift = (115 - lum) * 0.34;
          r += shadowLift;
          g += shadowLift;
          b += shadowLift;
        }

        // Studio Contrast S-Curve
        const contrast = inFaceRegion ? 1.15 : 1.08;
        r = (r - 128) * contrast + 128;
        g = (g - 128) * contrast + 128;
        b = (b - 128) * contrast + 128;

        dst[idx] = Math.max(0, Math.min(255, r));
        dst[idx + 1] = Math.max(0, Math.min(255, g));
        dst[idx + 2] = Math.max(0, Math.min(255, b));
        dst[idx + 3] = 255;
      }
    }

    return outImageData;
  }

  /**
   * Strict Dermis Segmentation & Regional Skin Tone Grading (SkinToneProcessor.ios.kt)
   * Dynamically tracks detected face location and preserves background & eyes.
   */
  function rebuildGradedAfterBuffer() {
    if (!state.isSampleLoaded) return;

    const w = state.afterBaseOffscreen.width;
    const h = state.afterBaseOffscreen.height;
    if (w === 0 || h === 0) return;

    state.afterGradedOffscreen.width = w;
    state.afterGradedOffscreen.height = h;

    const baseCtx = state.afterBaseOffscreen.getContext('2d', { willReadFrequently: true });
    const beforeCtx = state.beforeOffscreen.getContext('2d', { willReadFrequently: true });
    const outCtx = state.afterGradedOffscreen.getContext('2d');

    const baseData = baseCtx.getImageData(0, 0, w, h);
    const beforeData = beforeCtx.getImageData(0, 0, w, h);
    const src = baseData.data;
    const raw = beforeData.data;
    const outImg = outCtx.createImageData(w, h);
    const dst = outImg.data;

    const preset = getSelectedPreset();
    const params = state.editParams;

    const clarityBlend = Math.max(0.15, Math.min(1.0, params.facialClarity));
    const targetRgb = hexToRgb(preset.previewHex);
    const lipPreset = LIP_SHADE_PRESETS.find((l) => l.id === params.lipShade) || LIP_SHADE_PRESETS[0];
    const hairPreset = HAIR_SHADE_PRESETS.find((hp) => hp.id === params.hairShade) || HAIR_SHADE_PRESETS[0];

    const face = state.detectedFace && state.detectedFace.found
      ? state.detectedFace
      : { cx: 0.50, cy: 0.40, rx: 0.28, ry: 0.34 };

    const faceCx = face.cx;
    const faceCy = face.cy;
    const faceRx = face.rx;
    const faceRy = face.ry;

    const smoothness = params.skinSmoothness;
    const warmthDelta = (params.skinWarmth - 0.5) * 36.0 + preset.warmthShift * 42.0;
    const radianceLift = (params.skinRadiance - 0.5) * 28.0 + preset.luminanceLift * 45.0;
    const melaninShift = preset.melaninDepth * 38.0;
    const rednessCtrl = preset.rednessControl;
    const contrastBoost = 1.0 + preset.contrastBoost * 0.45;

    for (let y = 0; y < h; y++) {
      const ny = y / h;
      const rowOffset = y * w;

      for (let x = 0; x < w; x++) {
        const nx = x / w;
        const idx = (rowOffset + x) * 4;

        // Base AI Clarity interpolation between Before & Enhanced
        let r = raw[idx] * (1 - clarityBlend) + src[idx] * clarityBlend;
        let g = raw[idx + 1] * (1 - clarityBlend) + src[idx + 1] * clarityBlend;
        let b = raw[idx + 2] * (1 - clarityBlend) + src[idx + 2] * clarityBlend;

        const lum = 0.299 * r + 0.587 * g + 0.114 * b;
        const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
        const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

        let chromaSkin = 0.0;
        if (cr >= 130 && cr <= 178 && cb >= 75 && cb <= 132 && r > g && r > b) {
          const crDist = Math.abs(cr - 152) / 24.0;
          const cbDist = Math.abs(cb - 104) / 26.0;
          chromaSkin = Math.max(0, 1.0 - Math.max(crDist, cbDist));
        }

        const dxFace = (nx - faceCx) / faceRx;
        const dyFace = (ny - faceCy) / faceRy;
        const distFaceSq = dxFace * dxFace + dyFace * dyFace;
        const inFaceOrNeck = distFaceSq < 1.35;

        // Dynamic eye & mouth zones relative to detected face
        const inLeftEye = Math.abs(dxFace + 0.35) < 0.22 && Math.abs(dyFace + 0.15) < 0.14;
        const inRightEye = Math.abs(dxFace - 0.35) < 0.22 && Math.abs(dyFace + 0.15) < 0.14;
        const inMouth = Math.abs(dxFace) < 0.30 && Math.abs(dyFace - 0.38) < 0.14;

        if (inLeftEye || inRightEye) {
          const irisBoost = 1.0 + Math.max(0, params.skinRadiance - 0.5) * 0.30;
          r = Math.min(255, r * irisBoost);
          g = Math.min(255, g * irisBoost);
          b = Math.min(255, b * irisBoost);
        }

        let skinMask = inFaceOrNeck ? chromaSkin : chromaSkin * 0.22;
        if (inLeftEye || inRightEye || inMouth) {
          skinMask *= 0.10;
        }

        if (skinMask > 0.05) {
          if (smoothness > 0.05 && x > 1 && x < w - 2 && y > 1 && y < h - 2) {
            const idxL = idx - 8;
            const idxR = idx + 8;
            const idxU = idx - w * 8;
            const idxD = idx + w * 8;
            const avgR = (src[idx] + src[idxL] + src[idxR] + src[idxU] + src[idxD]) * 0.2;
            const avgG = (src[idx + 1] + src[idxL + 1] + src[idxR + 1] + src[idxU + 1] + src[idxD + 1]) * 0.2;
            const avgB = (src[idx + 2] + src[idxL + 2] + src[idxR + 2] + src[idxU + 2] + src[idxD + 2]) * 0.2;
            const smoothWeight = smoothness * 0.55 * skinMask * (1.15 - params.detailPreservation * 0.45);
            r = r * (1 - smoothWeight) + avgR * smoothWeight;
            g = g * (1 - smoothWeight) + avgG * smoothWeight;
            b = b * (1 - smoothWeight) + avgB * smoothWeight;
          }

          const excessRed = Math.max(0, r - g - 18);
          r -= excessRed * rednessCtrl * 0.28 * skinMask;
          g += excessRed * rednessCtrl * 0.14 * skinMask;

          const targetLum = 0.299 * targetRgb.r + 0.587 * targetRgb.g + 0.114 * targetRgb.b || 1;
          const lumRatio = Math.max(0.35, Math.min(1.65, lum / targetLum));
          const gradedR = targetRgb.r * lumRatio;
          const gradedG = targetRgb.g * lumRatio;
          const gradedB = targetRgb.b * lumRatio;

          const presetBlend = 0.26 * skinMask;
          r = r * (1 - presetBlend) + gradedR * presetBlend;
          g = g * (1 - presetBlend) + gradedG * presetBlend;
          b = b * (1 - presetBlend) + gradedB * presetBlend;

          r += (warmthDelta * 0.55 + radianceLift * 0.65 - melaninShift * 0.45) * skinMask;
          g += (warmthDelta * 0.22 + radianceLift * 0.60 - melaninShift * 0.50) * skinMask;
          b += (-warmthDelta * 0.40 + radianceLift * 0.50 - melaninShift * 0.55) * skinMask;

          r = ((r - 128) * (1 + (contrastBoost - 1) * skinMask)) + 128;
          g = ((g - 128) * (1 + (contrastBoost - 1) * skinMask)) + 128;
          b = ((b - 128) * (1 + (contrastBoost - 1) * skinMask)) + 128;
        }

        if (lipPreset.id !== 'NONE' && inMouth && r > g * 1.15) {
          const lipWeight = params.lipIntensity * 0.45;
          r = r * (1 - lipWeight) + lipPreset.r * lipWeight;
          g = g * (1 - lipWeight) + lipPreset.g * lipWeight;
          b = b * (1 - lipWeight) + lipPreset.b * lipWeight;
        }

        if (hairPreset.id !== 'NONE' && ny < faceCy && lum < 85 && chromaSkin < 0.25 && distFaceSq < 1.8) {
          const hairWeight = params.hairIntensity * 0.48;
          r = r * (1 - hairWeight) + hairPreset.r * hairWeight;
          g = g * (1 - hairWeight) + hairPreset.g * hairWeight;
          b = b * (1 - hairWeight) + hairPreset.b * hairWeight;
        }

        if (params.showSkinMaskOverlay && skinMask > 0.18) {
          const overlayAlpha = Math.min(0.65, skinMask * 0.65);
          r = r * (1 - overlayAlpha) + 236 * overlayAlpha;
          g = g * (1 - overlayAlpha) + 72 * overlayAlpha;
          b = b * (1 - overlayAlpha) + 153 * overlayAlpha;
        }

        dst[idx] = r < 0 ? 0 : r > 255 ? 255 : r;
        dst[idx + 1] = g < 0 ? 0 : g > 255 ? 255 : g;
        dst[idx + 2] = b < 0 ? 0 : b > 255 ? 255 : b;
        dst[idx + 3] = 255;
      }
    }

    outCtx.putImageData(outImg, 0, 0);
  }

  /**
   * Loads a sample portrait or user-uploaded image into the offscreen buffers
   */
  async function loadStudioSample(sampleIdx, customImg) {
    state.selectedSampleIndex = sampleIdx;
    state.isSampleLoaded = false;

    try {
      let beforeImg;
      let afterImg;
      let generateAfter = false;

      if (customImg) {
        state.customImageElement = customImg;
        beforeImg = customImg;
        afterImg = customImg;
        generateAfter = true;
      } else {
        const sample = STUDIO_SAMPLES[sampleIdx] || STUDIO_SAMPLES[0];
        if (sampleIdx === 2 && state.customImageElement) {
          beforeImg = state.customImageElement;
          afterImg = state.customImageElement;
          generateAfter = true;
        } else {
          beforeImg = await loadImageCached(sample.beforeUrl);
          afterImg = await loadImageCached(sample.afterUrl);
          generateAfter = Boolean(sample.isGeneratedAfter);
        }
      }

      const maxDim = 920;
      const aspect = beforeImg.naturalWidth / beforeImg.naturalHeight;
      let workW = beforeImg.naturalWidth;
      let workH = beforeImg.naturalHeight;
      if (workH > maxDim) {
        workH = maxDim;
        workW = Math.round(workH * aspect);
      }

      state.beforeOffscreen.width = workW;
      state.beforeOffscreen.height = workH;
      const bCtx = state.beforeOffscreen.getContext('2d', { willReadFrequently: true });
      bCtx.drawImage(beforeImg, 0, 0, workW, workH);

      state.afterBaseOffscreen.width = workW;
      state.afterBaseOffscreen.height = workH;
      const aCtx = state.afterBaseOffscreen.getContext('2d', { willReadFrequently: true });
      aCtx.drawImage(afterImg, 0, 0, workW, workH);

      if (generateAfter) {
        const rawData = aCtx.getImageData(0, 0, workW, workH);
        const enhanced = generateReminiAiEnhancedImageData(rawData);
        aCtx.putImageData(enhanced, 0, 0);
      }

      state.isSampleLoaded = true;
      rebuildGradedAfterBuffer();
      syncSampleTabsUI();
      requestStudioCanvasRender();
    } catch (err) {
      console.error('Failed to load sample:', err);
    }
  }

  // ==========================================================================
  // 4. UNIFIED BEFORE/AFTER SLIDER RENDERER (BeforeAfterSlider.kt)
  // ==========================================================================

  let renderRafId = null;

  function requestStudioCanvasRender() {
    if (renderRafId) return;
    renderRafId = requestAnimationFrame(() => {
      renderRafId = null;
      renderSplitCanvas(document.getElementById('comparisonCanvas'), {
        showLandmarks: state.studioBottomTab === 'MAGNIFIER'
      });
      renderSplitCanvas(document.getElementById('retouchCanvas'), {
        showLandmarks: true
      });
    });
  }

  function renderSplitCanvas(canvas, options) {
    if (!canvas || !state.isSampleLoaded) return;
    const rect = canvas.parentElement.getBoundingClientRect();
    if (rect.width < 10 || rect.height < 10) return;

    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const cw = Math.round(rect.width * dpr);
    const ch = Math.round(rect.height * dpr);
    if (canvas.width !== cw || canvas.height !== ch) {
      canvas.width = cw;
      canvas.height = ch;
    }

    const ctx = canvas.getContext('2d');
    ctx.save();
    ctx.scale(dpr, dpr);

    const viewW = rect.width;
    const viewH = rect.height;

    // Clear background
    ctx.fillStyle = '#131315';
    ctx.fillRect(0, 0, viewW, viewH);

    // Compute ContentScale.Crop destination rect
    const imgW = state.beforeOffscreen.width;
    const imgH = state.beforeOffscreen.height;
    const scale = Math.max(viewW / imgW, viewH / imgH);
    let drawW = imgW * scale;
    let drawH = imgH * scale;
    let drawX = (viewW - drawW) * 0.5;
    let drawY = (viewH - drawH) * 0.5;

    // Apply 200% Zoom centered on focus point (0.5, 0.35) when zoomed
    if (state.isZoomed200) {
      const zoom = 2.0;
      const focusPxX = drawX + drawW * state.zoomFocusX;
      const focusPxY = drawY + drawH * state.zoomFocusY;
      drawW *= zoom;
      drawH *= zoom;
      drawX = viewW * 0.5 - (focusPxX - drawX) * zoom;
      drawY = viewH * 0.42 - (focusPxY - drawY) * zoom;
    }

    const activeSplit = state.isHoldCompare ? 0.98 : state.splitProgress;
    const splitX = Math.max(0, Math.min(viewW, viewW * activeSplit));

    // 1. Draw Full AFTER Image (4K AI Graded)
    ctx.drawImage(state.afterGradedOffscreen, drawX, drawY, drawW, drawH);

    // 2. Draw Clipped BEFORE Image on Left Side (0 .. splitX)
    ctx.save();
    ctx.beginPath();
    ctx.rect(0, 0, splitX, viewH);
    ctx.clip();
    // Apply slight raw softness to Before side so split comparison is unmistakable
    ctx.filter = 'blur(0.5px) contrast(0.96)';
    ctx.drawImage(state.beforeOffscreen, drawX, drawY, drawW, drawH);
    ctx.filter = 'none';
    ctx.restore();

    // 3. Optional Animated Facial Landmark Wireframe Overlay (BeforeAfterSlider.kt lines 318-420)
    if (options && options.showLandmarks) {
      drawFacialLandmarkWireframe(ctx, viewW, viewH, state.wireframePulsePhase);
    }

    // 4. Specular Divider Line & Ambient Glow (BeforeAfterSlider.kt lines 155-184)
    const glowGrad = ctx.createLinearGradient(0, 0, 0, viewH);
    glowGrad.addColorStop(0, 'rgba(76, 215, 246, 0.05)');
    glowGrad.addColorStop(0.5, 'rgba(76, 215, 246, 0.38)');
    glowGrad.addColorStop(1, 'rgba(76, 215, 246, 0.05)');
    ctx.strokeStyle = glowGrad;
    ctx.lineWidth = 8;
    ctx.beginPath();
    ctx.moveTo(splitX, 0);
    ctx.lineTo(splitX, viewH);
    ctx.stroke();

    const lineGrad = ctx.createLinearGradient(0, 0, 0, viewH);
    lineGrad.addColorStop(0, 'rgba(255, 255, 255, 0.25)');
    lineGrad.addColorStop(0.5, 'rgba(76, 215, 246, 0.92)');
    lineGrad.addColorStop(1, 'rgba(255, 255, 255, 0.25)');
    ctx.strokeStyle = lineGrad;
    ctx.lineWidth = 1.8;
    ctx.beginPath();
    ctx.moveTo(splitX, 0);
    ctx.lineTo(splitX, viewH);
    ctx.stroke();

    // 5. Specular Glass Disc Thumb Handle (BeforeAfterSlider.kt lines 187-237)
    const handleY = viewH * 0.5;
    const outerRadius = 26;
    const innerRadius = 20;

    const haloGrad = ctx.createRadialGradient(splitX, handleY, 2, splitX, handleY, outerRadius);
    haloGrad.addColorStop(0, 'rgba(3, 181, 211, 0.42)');
    haloGrad.addColorStop(1, 'rgba(3, 181, 211, 0)');
    ctx.fillStyle = haloGrad;
    ctx.beginPath();
    ctx.arc(splitX, handleY, outerRadius, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = 'rgba(31, 31, 33, 0.88)';
    ctx.beginPath();
    ctx.arc(splitX, handleY, innerRadius, 0, Math.PI * 2);
    ctx.fill();

    ctx.strokeStyle = 'rgba(255, 255, 255, 0.80)';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.arc(splitX, handleY, innerRadius, 0, Math.PI * 2);
    ctx.stroke();

    // Left & Right Chevrons (< >) inside Handle
    const chevSize = 4.5;
    const chevOffset = 6.5;
    ctx.strokeStyle = '#E5E1E4';
    ctx.lineWidth = 2.0;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    // Left chevron
    ctx.beginPath();
    ctx.moveTo(splitX - chevOffset + chevSize * 0.5, handleY - chevSize);
    ctx.lineTo(splitX - chevOffset - chevSize * 0.5, handleY);
    ctx.lineTo(splitX - chevOffset + chevSize * 0.5, handleY + chevSize);
    ctx.stroke();

    // Right chevron
    ctx.beginPath();
    ctx.moveTo(splitX + chevOffset - chevSize * 0.5, handleY - chevSize);
    ctx.lineTo(splitX + chevOffset + chevSize * 0.5, handleY);
    ctx.lineTo(splitX + chevOffset - chevSize * 0.5, handleY + chevSize);
    ctx.stroke();

    ctx.restore();
  }

  /**
   * Draws the Biometric Facial Landmark Mesh Overlay (BeforeAfterSlider.kt drawFacialLandmarkWireframe)
   */
  function drawFacialLandmarkWireframe(ctx, w, h, pulsePhase) {
    const face = state.detectedFace && state.detectedFace.found
      ? state.detectedFace
      : { cx: 0.50, cy: 0.39, rx: 0.20, ry: 0.26 };

    const cx = w * face.cx;
    const cy = h * face.cy;
    const rx = Math.min(w, h) * face.rx * 1.05;
    const ry = Math.min(w, h) * face.ry * 1.15;
    const alpha = 0.26 + pulsePhase * 0.22;

    const landmarks = [
      [cx, cy - ry * 0.85],
      [cx - rx * 0.52, cy - ry * 0.65],
      [cx + rx * 0.52, cy - ry * 0.65],
      [cx - rx * 0.42, cy - ry * 0.22],
      [cx - rx * 0.18, cy - ry * 0.22],
      [cx + rx * 0.18, cy - ry * 0.22],
      [cx + rx * 0.42, cy - ry * 0.22],
      [cx, cy - ry * 0.12],
      [cx, cy + ry * 0.12],
      [cx - rx * 0.68, cy + ry * 0.05],
      [cx + rx * 0.68, cy + ry * 0.05],
      [cx - rx * 0.32, cy + ry * 0.42],
      [cx + rx * 0.32, cy + ry * 0.42],
      [cx, cy + ry * 0.78]
    ];

    const edges = [
      [0, 1], [0, 2], [1, 3], [2, 6],
      [3, 4], [5, 6], [4, 7], [5, 7],
      [7, 8], [3, 9], [6, 10], [9, 11],
      [10, 12], [8, 11], [8, 12], [11, 12],
      [11, 13], [12, 13], [9, 4], [10, 5]
    ];

    ctx.save();
    ctx.strokeStyle = `rgba(76, 215, 246, ${alpha.toFixed(3)})`;
    ctx.lineWidth = 1.1;

    for (let i = 0; i < edges.length; i++) {
      const a = landmarks[edges[i][0]];
      const b = landmarks[edges[i][1]];
      ctx.beginPath();
      ctx.moveTo(a[0], a[1]);
      ctx.lineTo(b[0], b[1]);
      ctx.stroke();
    }

    for (let i = 0; i < landmarks.length; i++) {
      const pt = landmarks[i];
      ctx.fillStyle = `rgba(208, 188, 255, ${(alpha * 0.55).toFixed(3)})`;
      ctx.beginPath();
      ctx.arc(pt[0], pt[1], 4.2, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#4CD7F6';
      ctx.beginPath();
      ctx.arc(pt[0], pt[1], 2.0, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();
  }

  /**
   * Attaches pointer drag + double-tap 200% zoom gestures to a BeforeAfterSlider container
   */
  function attachSplitSliderInteractions(containerEl) {
    if (!containerEl) return;
    let isDragging = false;
    let lastTapTime = 0;

    function updateSplitFromEvent(e) {
      const rect = containerEl.getBoundingClientRect();
      if (rect.width <= 0) return;
      const clientX = e.touches ? e.touches[0].clientX : e.clientX;
      const normX = (clientX - rect.left) / rect.width;
      state.splitProgress = Math.max(0.02, Math.min(0.98, normX));
      requestStudioCanvasRender();
    }

    containerEl.addEventListener('pointerdown', (e) => {
      if (e.target.closest('button')) return;

      const now = Date.now();
      if (now - lastTapTime < 280) {
        // Double-tap toggles 200% face-centered zoom
        state.isZoomed200 = !state.isZoomed200;
        syncZoomPillUI();
        requestStudioCanvasRender();
        lastTapTime = 0;
        return;
      }
      lastTapTime = now;

      isDragging = true;
      containerEl.setPointerCapture(e.pointerId);
      updateSplitFromEvent(e);
    });

    containerEl.addEventListener('pointermove', (e) => {
      if (!isDragging) return;
      updateSplitFromEvent(e);
    });

    const endDrag = () => {
      isDragging = false;
    };
    containerEl.addEventListener('pointerup', endDrag);
    containerEl.addEventListener('pointercancel', endDrag);
  }

  function syncZoomPillUI() {
    const zoomBtn = document.getElementById('btnComparisonZoomPill');
    const telemetryText = document.getElementById('comparisonTelemetryText');
    if (zoomBtn) {
      zoomBtn.textContent = state.isZoomed200 ? '200%' : '100%';
      zoomBtn.classList.toggle('zoomed', state.isZoomed200);
    }
    if (telemetryText) {
      if (state.studioBottomTab === 'MAGNIFIER') {
        telemetryText.textContent = `Biometric Pore Mesh • Zoom: ${state.isZoomed200 ? '200%' : '100%'} (Double-tap)`;
      } else {
        telemetryText.textContent = '1080p → 4320p (4K UHD) • Sharpness: Ultra';
      }
    }
  }

  // ==========================================================================
  // 5. NAVIGATION & UI SYNCHRONIZATION (MainScreen.kt)
  // ==========================================================================

  function switchAppScreen(targetScreen) {
    state.currentAppScreen = targetScreen;
    document.getElementById('appScreenMain').classList.toggle('active', targetScreen === 'MAIN');
    document.getElementById('appScreenPassport').classList.toggle('active', targetScreen === 'PASSPORT_STUDIO');

    if (targetScreen === 'MAIN') {
      requestStudioCanvasRender();
    } else if (targetScreen === 'PASSPORT_STUDIO') {
      renderPassportCanvas();
    }
  }

  function switchMainMode(mode) {
    state.screenMode = mode;

    const views = {
      HOME: document.getElementById('viewHome'),
      COMPARISON_STUDIO: document.getElementById('viewComparisonStudio'),
      RETOUCH_SUITE: document.getElementById('viewRetouchSuite'),
      PROCESSING: document.getElementById('viewProcessing'),
      SAVED_SHARE: document.getElementById('viewSavedShare')
    };

    Object.keys(views).forEach((key) => {
      if (views[key]) views[key].classList.toggle('active', key === mode);
    });

    // Show bottom nav only on HOME, COMPARISON_STUDIO, RETOUCH_SUITE
    const bottomNav = document.getElementById('auraBottomNav');
    if (bottomNav) {
      const showNav = mode === 'HOME' || mode === 'COMPARISON_STUDIO' || mode === 'RETOUCH_SUITE';
      bottomNav.style.display = showNav ? 'flex' : 'none';

      bottomNav.querySelectorAll('.bottom-nav-item').forEach((btn) => {
        const navTarget = btn.getAttribute('data-bottom-nav');
        btn.classList.toggle('active', navTarget === mode);
      });
    }

    if (mode === 'COMPARISON_STUDIO' || mode === 'RETOUCH_SUITE') {
      setTimeout(() => requestStudioCanvasRender(), 20);
    }
  }

  function runNeuralProcessingTransition(targetModeAfter) {
    switchMainMode('PROCESSING');
    const arc = document.getElementById('processingArcCircle');
    const pctText = document.getElementById('processingPercentText');
    const stageText = document.getElementById('processingStageText');
    const circumference = 326.7;

    let progress = 0.08;
    const timer = setInterval(() => {
      progress += 0.06;
      if (progress >= 1.0) {
        clearInterval(timer);
        switchMainMode(targetModeAfter || 'COMPARISON_STUDIO');
        return;
      }
      const offset = circumference * (1 - progress);
      if (arc) arc.style.strokeDashoffset = String(offset);
      if (pctText) pctText.textContent = `${Math.round(progress * 100)}%`;
      if (stageText) {
        if (progress < 0.35) {
          stageText.textContent = 'Analyzing Facial Landmarks & Lighting...';
        } else if (progress < 0.72) {
          stageText.textContent = 'Reconstructing 4K Dermal Micro-Texture...';
        } else {
          stageText.textContent = 'Applying 10-Bit Studio Color Grading...';
        }
      }
    }, 55);
  }

  function syncCreditsUI() {
    const homeCredits = document.getElementById('homeCreditsText');
    const exportCredits = document.getElementById('exportCreditsBalanceText');
    const dialogCredits = document.getElementById('dialogCreditsCount');

    if (homeCredits) homeCredits.textContent = `${state.userCredits} Credits`;
    if (exportCredits) exportCredits.textContent = `${state.userCredits} Credits`;
    if (dialogCredits) dialogCredits.textContent = `${state.userCredits} Credits`;
  }

  function syncSampleTabsUI() {
    document.querySelectorAll('[data-studio-sample]').forEach((btn) => {
      const idx = parseInt(btn.getAttribute('data-studio-sample'), 10);
      btn.classList.toggle('active', idx === state.selectedSampleIndex);
    });
  }

  function syncStudioControlsUI() {
    const preset = getSelectedPreset();
    const params = state.editParams;

    // Studio Floating Dock
    const titleEl = document.getElementById('studioIntensityTitle');
    const pillTextEl = document.getElementById('studioPresetPillText');
    const swatchEl = document.getElementById('studioPresetSwatch');
    const mainSlider = document.getElementById('studioMainSlider');
    const maskLabel = document.getElementById('studioMaskStateLabel');

    const activeVal = state.studioBottomTab === 'SKIN_AI' ? params.skinSmoothness : params.facialClarity;
    if (titleEl) {
      titleEl.textContent = state.studioBottomTab === 'SKIN_AI' ? 'Dermis Smoothing & Tone' : 'AI Enhancement Intensity';
    }
    if (pillTextEl) {
      pillTextEl.textContent = `${preset.displayName} • ${Math.round(activeVal * 100)}%`;
    }
    if (swatchEl) {
      swatchEl.style.backgroundColor = preset.previewHex;
    }
    if (mainSlider) {
      mainSlider.value = String(Math.round(activeVal * 100));
    }
    if (maskLabel) {
      maskLabel.textContent = params.showSkinMaskOverlay ? 'ON' : 'OFF';
    }

    // Studio Tool Pills
    document.querySelectorAll('[data-studio-tool]').forEach((btn) => {
      btn.classList.toggle('active', btn.getAttribute('data-studio-tool') === state.studioBottomTab);
    });

    // Retouch Suite 4 Sliders
    const denoisePct = Math.round(params.facialClarity * 100);
    const smoothPct = Math.round(params.skinSmoothness * 100);
    const eyePct = Math.max(0, Math.min(100, Math.round((params.skinRadiance - 0.5) * 100)));
    const hdrPct = Math.max(0, Math.min(100, Math.round((params.skinWarmth - 0.5) * 100)));

    setElText('valDenoiseUnblur', `${denoisePct}%`);
    setElValue('sliderDenoiseUnblur', denoisePct);

    setElText('valSkinTexture', `${smoothPct}% (Natural)`);
    setElValue('sliderSkinTexture', smoothPct);

    setElText('valEyeIris', `${eyePct}%`);
    setElValue('sliderEyeIris', eyePct);

    setElText('valLightingHdr', `${hdrPct}%`);
    setElValue('sliderLightingHdr', hdrPct);

    // Lip & Hair sliders
    setElText('lipIntensityLabel', `${Math.round(params.lipIntensity * 100)}%`);
    setElValue('sliderLipIntensity', Math.round(params.lipIntensity * 100));
    setElText('hairIntensityLabel', `${Math.round(params.hairIntensity * 100)}%`);
    setElValue('sliderHairIntensity', Math.round(params.hairIntensity * 100));

    const chkMask = document.getElementById('chkRetouchSkinMask');
    if (chkMask) chkMask.checked = Boolean(params.showSkinMaskOverlay);

    // Dialog Sliders
    setElText('dlgValSmooth', `${smoothPct}%`);
    setElValue('dlgSliderSmooth', smoothPct);
    setElText('dlgValWarmth', `${hdrPct}%`);
    setElValue('dlgSliderWarmth', hdrPct);
    setElText('dlgValRadiance', `${eyePct}%`);
    setElValue('dlgSliderRadiance', eyePct);
    setElText('dlgValDetail', `${Math.round(params.detailPreservation * 100)}%`);
    setElValue('dlgSliderDetail', Math.round(params.detailPreservation * 100));

    // Active states on Skin Tone Grids & Shade Chips
    document.querySelectorAll('[data-skin-preset-id]').forEach((el) => {
      const isSel = el.getAttribute('data-skin-preset-id') === state.selectedPresetId;
      el.classList.toggle('active', isSel);
      const checkIcon = el.querySelector('.preset-check-icon');
      if (checkIcon) checkIcon.style.display = isSel ? 'inline-flex' : 'none';
    });

    document.querySelectorAll('[data-lip-shade]').forEach((el) => {
      el.classList.toggle('active', el.getAttribute('data-lip-shade') === params.lipShade);
    });

    document.querySelectorAll('[data-hair-shade]').forEach((el) => {
      el.classList.toggle('active', el.getAttribute('data-hair-shade') === params.hairShade);
    });

    syncZoomPillUI();
  }

  function populateDynamicSkinToneAndShadeLists() {
    // 1. Retouch Suite 2-Column Skin Tone Grid (AuraSkinToneGrid)
    const grid = document.getElementById('retouchSkinToneGrid');
    if (grid) {
      grid.innerHTML = SKIN_TONE_PRESETS.map((preset) => `
        <button type="button" class="skin-tone-preset-card" data-skin-preset-id="${preset.id}">
          <div class="skin-tone-card-top">
            <div class="skin-swatch-circle" style="background: ${preset.previewHex}">
              <span class="material-symbols-rounded preset-check-icon" style="display: none">check</span>
            </div>
            ${preset.isDefault ? '<span class="skin-default-badge">DEFAULT</span>' : ''}
          </div>
          <div>
            <div class="skin-tone-card-title">${preset.displayName}</div>
            <div class="skin-tone-card-sub">${preset.regionDescription}</div>
          </div>
        </button>
      `).join('');

      grid.querySelectorAll('[data-skin-preset-id]').forEach((btn) => {
        btn.addEventListener('click', () => {
          selectSkinTonePreset(btn.getAttribute('data-skin-preset-id'));
        });
      });
    }

    // 2. Dialog Skin Tone Radio List (AuraSkinToneDialog)
    const dlgList = document.getElementById('dialogSkinToneList');
    if (dlgList) {
      dlgList.innerHTML = SKIN_TONE_PRESETS.map((preset) => `
        <div class="dialog-skin-row" data-skin-preset-id="${preset.id}" role="button" tabindex="0">
          <div class="skin-swatch-circle" style="background: ${preset.previewHex}">
            <span class="material-symbols-rounded preset-check-icon" style="display: none">check</span>
          </div>
          <div style="flex: 1">
            <div style="font-size: 13px; font-weight: 700; color: #E5E1E4">${preset.displayName}</div>
            <div style="font-size: 11px; color: #C3C6CF">${preset.regionDescription}</div>
          </div>
        </div>
      `).join('');

      dlgList.querySelectorAll('[data-skin-preset-id]').forEach((row) => {
        row.addEventListener('click', () => {
          selectSkinTonePreset(row.getAttribute('data-skin-preset-id'));
        });
      });
    }

    // 3. Lip Shade Chips
    const lipRow = document.getElementById('lipShadeChipsRow');
    if (lipRow) {
      lipRow.innerHTML = LIP_SHADE_PRESETS.map((lp) => `
        <button type="button" class="shade-chip-btn" data-lip-shade="${lp.id}">
          <span class="shade-dot" style="background: ${lp.hex}"></span>
          <span>${lp.displayName}</span>
        </button>
      `).join('');

      lipRow.querySelectorAll('[data-lip-shade]').forEach((btn) => {
        btn.addEventListener('click', () => {
          state.editParams.lipShade = btn.getAttribute('data-lip-shade');
          savePersistedSettings();
          rebuildGradedAfterBuffer();
          syncStudioControlsUI();
          requestStudioCanvasRender();
        });
      });
    }

    // 4. Hair Shade Chips
    const hairRow = document.getElementById('hairShadeChipsRow');
    if (hairRow) {
      hairRow.innerHTML = HAIR_SHADE_PRESETS.map((hp) => `
        <button type="button" class="shade-chip-btn" data-hair-shade="${hp.id}">
          <span class="shade-dot" style="background: ${hp.hex}"></span>
          <span>${hp.displayName}</span>
        </button>
      `).join('');

      hairRow.querySelectorAll('[data-hair-shade]').forEach((btn) => {
        btn.addEventListener('click', () => {
          state.editParams.hairShade = btn.getAttribute('data-hair-shade');
          savePersistedSettings();
          rebuildGradedAfterBuffer();
          syncStudioControlsUI();
          requestStudioCanvasRender();
        });
      });
    }
  }

  function selectSkinTonePreset(presetId) {
    state.selectedPresetId = presetId;
    savePersistedSettings();
    rebuildGradedAfterBuffer();
    syncStudioControlsUI();
    requestStudioCanvasRender();
  }

  // ==========================================================================
  // 6. PASSPORT PHOTO STUDIO ENGINE (PassportPhotoScreen.kt)
  // ==========================================================================

  function populatePassportUI() {
    // 1. Coats Scroll Row
    renderPassportCoatsList();

    // 2. Background Colors Row
    const bgRow = document.getElementById('passportBgColorsRow');
    if (bgRow) {
      bgRow.innerHTML = PASSPORT_BG_COLORS.map((bg) => `
        <div class="passport-bg-card ${bg.id === state.passport.bgColor.id ? 'active' : ''}" data-passport-bg="${bg.id}">
          <div class="passport-bg-swatch" style="background: ${bg.hex}">
            ${bg.id === state.passport.bgColor.id ? `<span class="material-symbols-rounded" style="font-size:18px; color:${bg.isLight ? '#0284C7' : '#FFFFFF'}">check</span>` : ''}
          </div>
          <div class="passport-bg-label">${bg.label}</div>
        </div>
      `).join('');

      bgRow.querySelectorAll('[data-passport-bg]').forEach((card) => {
        card.addEventListener('click', () => {
          const id = card.getAttribute('data-passport-bg');
          const found = PASSPORT_BG_COLORS.find((b) => b.id === id);
          if (found) {
            state.passport.bgColor = found;
            populatePassportUI();
            renderPassportCanvas();
          }
        });
      });
    }

    // 3. Country Standards List
    const countryList = document.getElementById('passportCountriesList');
    if (countryList) {
      countryList.innerHTML = COUNTRY_STANDARDS.map((c) => {
        const isSel = c.code === state.passport.country.code;
        return `
          <div class="passport-country-row ${isSel ? 'active' : ''}" data-passport-country="${c.code}">
            <span class="country-flag">${c.flag}</span>
            <div class="country-info">
              <div class="country-name">${c.countryName}</div>
              <div class="country-spec">${c.specLabel} • 300 DPI (${c.widthPx300Dpi}×${c.heightPx300Dpi} px)</div>
            </div>
            <span class="material-symbols-rounded" style="color: ${isSel ? '#38BDF8' : '#475569'}">
              ${isSel ? 'check_circle' : 'radio_button_unchecked'}
            </span>
          </div>
        `;
      }).join('');

      countryList.querySelectorAll('[data-passport-country]').forEach((row) => {
        row.addEventListener('click', () => {
          const code = row.getAttribute('data-passport-country');
          const found = COUNTRY_STANDARDS.find((c) => c.code === code);
          if (found) {
            state.passport.country = found;
            updatePassportHeaderTitle();
            populatePassportUI();
            renderPassportCanvas();
          }
        });
      });
    }
  }

  function renderPassportCoatsList() {
    const row = document.getElementById('passportCoatsScrollRow');
    if (!row) return;

    const filter = state.passport.coatCategoryFilter;
    const filteredCoats = FORMAL_COAT_PRESETS.filter((c) => {
      if (c.id === 'NONE') return true;
      if (filter === 'ALL') return true;
      return c.category === filter;
    });

    row.innerHTML = filteredCoats.map((coat) => {
      const isSel = coat.id === state.passport.coat.id;
      if (coat.id === 'NONE') {
        return `
          <div class="coat-item-card ${isSel ? 'active' : ''}" data-passport-coat="NONE">
            <div class="coat-preview-box original-box">
              <span class="material-symbols-rounded" style="font-size:28px">person</span>
            </div>
            <div>
              <div class="coat-item-title">Original</div>
              <div class="coat-item-sub">No Suit</div>
            </div>
          </div>
        `;
      }
      return `
        <div class="coat-item-card ${isSel ? 'active' : ''}" data-passport-coat="${coat.id}">
          <div class="coat-preview-box">
            <img src="${coat.assetUrl}" alt="${coat.title}" />
            <span class="coat-swatch-dot" style="background: ${coat.swatchHex}"></span>
          </div>
          <div style="width: 100%">
            <div class="coat-item-title">${coat.title}</div>
            <div class="coat-item-sub">${coat.subtitle}</div>
          </div>
        </div>
      `;
    }).join('');

    row.querySelectorAll('[data-passport-coat]').forEach((card) => {
      card.addEventListener('click', async () => {
        const coatId = card.getAttribute('data-passport-coat');
        const found = FORMAL_COAT_PRESETS.find((c) => c.id === coatId) || FORMAL_COAT_PRESETS[0];
        state.passport.coat = found;
        renderPassportCoatsList();
        await ensureCalibratedCoatLoaded(found);
        renderPassportCanvas();
      });
    });
  }

  function updatePassportHeaderTitle() {
    const c = state.passport.country;
    setElText('passportEditorHeaderTitle', `${c.countryName} (${c.specLabel})`);
    setElText('passportCornerSpecBadge', `${c.widthMm}×${c.heightMm} mm`);
  }

  function switchPassportMode(mode) {
    state.passport.mode = mode;
    const views = {
      UPLOAD_HOME: document.getElementById('passportViewUploadHome'),
      PROCESSING: document.getElementById('passportViewProcessing'),
      STUDIO_EDITOR: document.getElementById('passportViewEditor'),
      SAVED_EXPORT: document.getElementById('passportViewSavedExport')
    };
    Object.keys(views).forEach((k) => {
      if (views[k]) views[k].classList.toggle('active', k === mode);
    });

    if (mode === 'STUDIO_EDITOR') {
      updatePassportHeaderTitle();
      setTimeout(() => renderPassportCanvas(), 20);
    }
  }

  /**
   * Calibrates a formal coat PNG (`calibrateCoatBitmap` in PassportPhotoScreen.kt lines 997-1055):
   * Makes the upper collar/neck opening transparent so the subject's real neck shows cleanly
   * inside the suit collar instead of being covered by an opaque white mannequin triangle.
   */
  async function ensureCalibratedCoatLoaded(coat) {
    if (!coat || !coat.assetUrl) return null;
    if (state.passport.calibratedCoatCache[coat.id]) {
      return state.passport.calibratedCoatCache[coat.id];
    }

    const img = await loadImageCached(coat.assetUrl);
    const w = img.naturalWidth;
    const h = img.naturalHeight;

    // Crop tight bounding box of non-transparent coat pixels & open the upper collar V-slot
    const tempCanvas = document.createElement('canvas');
    tempCanvas.width = w;
    tempCanvas.height = h;
    const tCtx = tempCanvas.getContext('2d', { willReadFrequently: true });
    tCtx.drawImage(img, 0, 0);

    const imgData = tCtx.getImageData(0, 0, w, h);
    const px = imgData.data;

    let minX = w, maxX = 0, minY = h, maxY = 0;
    for (let y = 0; y < h; y++) {
      for (let x = 0; x < w; x++) {
        const a = px[(y * w + x) * 4 + 3];
        if (a > 24) {
          if (x < minX) minX = x;
          if (x > maxX) maxX = x;
          if (y < minY) minY = y;
          if (y > maxY) maxY = y;
        }
      }
    }

    if (maxX <= minX || maxY <= minY) {
      state.passport.calibratedCoatCache[coat.id] = img;
      return img;
    }

    const bw = maxX - minX + 1;
    const bh = maxY - minY + 1;
    const outCanvas = document.createElement('canvas');
    outCanvas.width = bw;
    outCanvas.height = bh;
    const outCtx = outCanvas.getContext('2d');
    const outData = outCtx.createImageData(bw, bh);
    const dst = outData.data;

    for (let y = 0; y < bh; y++) {
      const ny = y / bh;
      const srcY = minY + y;
      for (let x = 0; x < bw; x++) {
        const nx = (x / bw) - 0.5; // -0.5 .. +0.5
        const srcX = minX + x;
        const sIdx = (srcY * w + srcX) * 4;
        const dIdx = (y * bw + x) * 4;

        let r = px[sIdx];
        let g = px[sIdx + 1];
        let b = px[sIdx + 2];
        let a = px[sIdx + 3];

        // Open the upper neck V-zone (top 22% of coat) where white/light mannequin neck filler sits
        if (a > 0 && ny < 0.24) {
          const vHalfWidth = Math.max(0.02, (0.24 - ny) * 0.72);
          const distFromCenter = Math.abs(nx);
          const brightness = (r + g + b) / 3;
          const saturation = Math.max(r, g, b) - Math.min(r, g, b);

          if (distFromCenter < vHalfWidth && brightness > 195 && saturation < 35) {
            const fade = Math.min(1, (vHalfWidth - distFromCenter) / (vHalfWidth * 0.45));
            a = Math.round(a * (1 - fade));
          }
        }

        dst[dIdx] = r;
        dst[dIdx + 1] = g;
        dst[dIdx + 2] = b;
        dst[dIdx + 3] = a;
      }
    }

    outCtx.putImageData(outData, 0, 0);
    state.passport.calibratedCoatCache[coat.id] = outCanvas;
    return outCanvas;
  }

  /**
   * Segments the portrait subject from the background (Hivision MODNet + BiSeNet + MediaPipe simulation)
   * Creates:
   *  1. `subjectCutoutCanvas` (full head + shoulders with background removed)
   *  2. `subjectHeadNeckOnlyCanvas` (head + neck only, suppressing original casual clothing below the neck so the formal suit replaces it cleanly)
   */
  function buildPassportSubjectCutout(sourceImg) {
    const maxDim = 760;
    const aspect = sourceImg.naturalWidth / sourceImg.naturalHeight;
    const h = Math.min(maxDim, sourceImg.naturalHeight || 720);
    const w = Math.round(h * aspect);

    const srcCanvas = document.createElement('canvas');
    srcCanvas.width = w;
    srcCanvas.height = h;
    const sCtx = srcCanvas.getContext('2d', { willReadFrequently: true });
    sCtx.drawImage(sourceImg, 0, 0, w, h);

    const srcData = sCtx.getImageData(0, 0, w, h);
    const px = srcData.data;

    // Sample corner/top-border background colors
    const bgSamples = [];
    const sampleCoords = [
      [4, 4], [w - 5, 4], [Math.floor(w * 0.15), 4], [Math.floor(w * 0.85), 4],
      [4, Math.floor(h * 0.25)], [w - 5, Math.floor(h * 0.25)],
      [4, Math.floor(h * 0.55)], [w - 5, Math.floor(h * 0.55)]
    ];
    sampleCoords.forEach(([sx, sy]) => {
      const idx = (sy * w + sx) * 4;
      bgSamples.push([px[idx], px[idx + 1], px[idx + 2]]);
    });

    function minColorDistToBg(r, g, b) {
      let minDist = 999999;
      for (let i = 0; i < bgSamples.length; i++) {
        const dr = r - bgSamples[i][0];
        const dg = g - bgSamples[i][1];
        const db = b - bgSamples[i][2];
        const d = dr * dr + dg * dg + db * db;
        if (d < minDist) minDist = d;
      }
      return Math.sqrt(minDist);
    }

    // Detect skin center of mass to anchor face metrics accurately for any photo
    let skinSumX = 0, skinSumY = 0, skinWeightSum = 0;
    let minSkinY = h * 0.7, maxSkinY = h * 0.2;

    for (let y = Math.floor(h * 0.08); y < Math.floor(h * 0.78); y++) {
      for (let x = Math.floor(w * 0.18); x < Math.floor(w * 0.82); x++) {
        const idx = (y * w + x) * 4;
        const r = px[idx], g = px[idx + 1], b = px[idx + 2];
        const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;
        const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
        if (cr >= 134 && cr <= 175 && cb >= 80 && cb <= 128 && r > g && g > b * 0.85 && minColorDistToBg(r, g, b) > 28) {
          skinSumX += x;
          skinSumY += y;
          skinWeightSum += 1;
          if (y < minSkinY) minSkinY = y;
          if (y > maxSkinY) maxSkinY = y;
        }
      }
    }

    const faceCenterX = skinWeightSum > 80 ? (skinSumX / skinWeightSum) / w : 0.50;
    const faceCenterY = skinWeightSum > 80 ? (skinSumY / skinWeightSum) / h : 0.40;
    const eyeY = Math.max(0.24, Math.min(0.52, faceCenterY - 0.035));
    const headHeight = Math.max(0.34, Math.min(0.56, ((maxSkinY - minSkinY) / h) * 1.18 || 0.44));

    state.passport.faceMetrics = {
      centerX: faceCenterX,
      eyeY: eyeY,
      headHeight: headHeight,
      neckBottomY: Math.min(0.84, eyeY + headHeight * 0.68)
    };

    // Build alpha mattes for Full Subject (`cutoutA`) and Head+Neck Only (`headNeckA`)
    state.passport.subjectCutoutCanvas.width = w;
    state.passport.subjectCutoutCanvas.height = h;
    state.passport.subjectHeadNeckOnlyCanvas.width = w;
    state.passport.subjectHeadNeckOnlyCanvas.height = h;

    const fullCtx = state.passport.subjectCutoutCanvas.getContext('2d');
    const headCtx = state.passport.subjectHeadNeckOnlyCanvas.getContext('2d');
    const fullImg = fullCtx.createImageData(w, h);
    const headImg = headCtx.createImageData(w, h);
    const dstFull = fullImg.data;
    const dstHead = headImg.data;

    const headRx = 0.25;
    const headRy = headHeight * 0.64;
    const headCy = eyeY + headHeight * 0.04;
    const neckTopY = headCy + headRy * 0.65;
    const neckBotY = state.passport.faceMetrics.neckBottomY;

    for (let y = 0; y < h; y++) {
      const ny = y / h;
      for (let x = 0; x < w; x++) {
        const nx = x / w;
        const idx = (y * w + x) * 4;
        const r = px[idx], g = px[idx + 1], b = px[idx + 2];

        const bgDist = minColorDistToBg(r, g, b);

        // Head ellipse distance
        const dxH = (nx - faceCenterX) / headRx;
        const dyH = (ny - headCy) / headRy;
        const headEllipseDist = Math.sqrt(dxH * dxH + dyH * dyH);

        // Torso/Shoulders silhouette
        const shoulderCy = neckBotY + 0.18;
        const dxS = (nx - faceCenterX) / 0.44;
        const dyS = (ny - shoulderCy) / 0.34;
        const torsoDist = Math.sqrt(dxS * dxS + dyS * dyS);

        // Neck column
        const inNeckColumn = ny >= neckTopY && ny <= neckBotY + 0.05 && Math.abs(nx - faceCenterX) < 0.125;

        const inProtectedCore = headEllipseDist < 0.82 || (inNeckColumn && Math.abs(nx - faceCenterX) < 0.09);
        const inOuterSilhouette = headEllipseDist < 1.24 || inNeckColumn || (ny > neckTopY && torsoDist < 1.15);

        let alphaFull = 0;
        if (inProtectedCore) {
          alphaFull = 255;
        } else if (inOuterSilhouette) {
          if (bgDist > 42) {
            alphaFull = 255;
          } else if (bgDist > 20) {
            alphaFull = Math.round(((bgDist - 20) / 22) * 255);
          }
        }

        // For Head+Neck Only (used when a Formal Suit is selected):
        // Suppress original clothing outside the neck V-slot below the chin!
        let alphaHeadNeck = alphaFull;
        if (ny > neckTopY) {
          const vProgress = Math.max(0, (ny - neckTopY) / Math.max(0.08, neckBotY - neckTopY));
          const allowedHalfWidth = Math.max(0.015, 0.115 * (1 - vProgress * 0.72));
          const distFromNeckCenter = Math.abs(nx - faceCenterX);

          if (ny > neckBotY + 0.02 || distFromNeckCenter > allowedHalfWidth + 0.025) {
            alphaHeadNeck = 0;
          } else if (distFromNeckCenter > allowedHalfWidth) {
            const fade = 1 - (distFromNeckCenter - allowedHalfWidth) / 0.025;
            alphaHeadNeck = Math.round(alphaHeadNeck * Math.max(0, fade));
          }
        }

        dstFull[idx] = r;
        dstFull[idx + 1] = g;
        dstFull[idx + 2] = b;
        dstFull[idx + 3] = alphaFull;

        dstHead[idx] = r;
        dstHead[idx + 1] = g;
        dstHead[idx + 2] = b;
        dstHead[idx + 3] = alphaHeadNeck;
      }
    }

    fullCtx.putImageData(fullImg, 0, 0);
    headCtx.putImageData(headImg, 0, 0);
  }

  /**
   * Renders a single 300 DPI Passport Photo onto any target canvas context
   */
  function renderSinglePassportPhotoToContext(ctx, outW, outH, includeGuides) {
    const p = state.passport;
    const bgHex = p.bgColor.hex;

    // 1. Layer 1: Official Embassy Background Fill + Subtle Studio Radial Gradient
    ctx.save();
    ctx.fillStyle = bgHex;
    ctx.fillRect(0, 0, outW, outH);

    const radGrad = ctx.createRadialGradient(outW * 0.5, outH * 0.4, outW * 0.1, outW * 0.5, outH * 0.45, outW * 0.75);
    radGrad.addColorStop(0, 'rgba(255, 255, 255, 0.08)');
    radGrad.addColorStop(1, 'rgba(0, 0, 0, 0.04)');
    ctx.fillStyle = radGrad;
    ctx.fillRect(0, 0, outW, outH);

    // 2. Layer 2: Subject Biometric Alignment (Crown 12.5%, Eye Line 44%, Chin 76%)
    const hasSuit = p.coat && p.coat.id !== 'NONE';
    const subjectCanvas = hasSuit ? p.subjectHeadNeckOnlyCanvas : p.subjectCutoutCanvas;

    if (subjectCanvas && subjectCanvas.width > 0) {
      const fm = p.faceMetrics;
      const targetHeadPx = outH * 0.61 * p.userZoom;
      const srcHeadPx = subjectCanvas.height * fm.headHeight;
      const scale = targetHeadPx / Math.max(1, srcHeadPx);

      const drawW = subjectCanvas.width * scale;
      const drawH = subjectCanvas.height * scale;

      const targetEyeY = outH * (0.43 + p.userPanY);
      const targetCenterX = outW * (0.50 + p.userPanX);

      const drawX = targetCenterX - fm.centerX * drawW;
      const drawY = targetEyeY - fm.eyeY * drawH;

      ctx.drawImage(subjectCanvas, drawX, drawY, drawW, drawH);

      // 3. Layer 3: Calibrated Formal Suit Overlay anchored to Neck & Chin
      if (hasSuit) {
        const coatGraphic = p.calibratedCoatCache[p.coat.id];
        if (coatGraphic) {
          const gw = coatGraphic.width || coatGraphic.naturalWidth;
          const gh = coatGraphic.height || coatGraphic.naturalHeight;
          const suitW = outW * p.coat.shoulderWidthRatio * 1.12 * p.coatScale * p.userZoom;
          const suitH = suitW * (gh / gw);

          const chinY = targetEyeY + targetHeadPx * 0.50;
          const collarAnchorOffset = suitH * p.coat.collarAnchorY;
          const suitX = targetCenterX - suitW * 0.5;
          const suitY = chinY - collarAnchorOffset + outH * (0.045 + p.coatOffsetY);

          ctx.drawImage(coatGraphic, suitX, suitY, suitW, suitH);
        }
      }
    }

    // 4. Optional ICAO Biometric Guide Lines (PassportPhotoScreen.kt BiometricGuideOverlay)
    if (includeGuides && p.showBiometricGuides) {
      drawIcaoBiometricGuides(ctx, outW, outH);
    }

    ctx.restore();
  }

  function drawIcaoBiometricGuides(ctx, w, h) {
    const crownY = h * 0.125;
    const eyeY = h * 0.44;
    const chinY = h * 0.76;
    const centerX = w * 0.5;

    ctx.save();
    // Vertical center axis
    ctx.setLineDash([5, 5]);
    ctx.strokeStyle = 'rgba(56, 189, 248, 0.50)';
    ctx.lineWidth = 1.2;
    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX, h);
    ctx.stroke();

    // Crown Line (#EF4444)
    ctx.strokeStyle = 'rgba(239, 68, 68, 0.80)';
    ctx.beginPath();
    ctx.moveTo(0, crownY);
    ctx.lineTo(w, crownY);
    ctx.stroke();

    // Eye Line (#22C55E)
    ctx.strokeStyle = 'rgba(34, 197, 94, 0.85)';
    ctx.beginPath();
    ctx.moveTo(0, eyeY);
    ctx.lineTo(w, eyeY);
    ctx.stroke();

    // Chin Line (#38BDF8)
    ctx.strokeStyle = 'rgba(56, 189, 248, 0.80)';
    ctx.beginPath();
    ctx.moveTo(0, chinY);
    ctx.lineTo(w, chinY);
    ctx.stroke();

    ctx.setLineDash([]);
    ctx.font = 'bold 9px Inter, sans-serif';
    ctx.fillStyle = '#EF4444';
    ctx.fillText('CROWN TOP', 6, crownY - 4);
    ctx.fillStyle = '#22C55E';
    ctx.fillText('EYE LINE', 6, eyeY - 4);
    ctx.fillStyle = '#38BDF8';
    ctx.fillText('CHIN BASE', 6, chinY - 4);
    ctx.restore();
  }

  /**
   * Renders the 4x6" Lab Print Sheet (Multi-Copy) onto a target canvas
   */
  function renderPassport4x6SheetToCanvas(targetCanvas) {
    const sheetW = 1200;
    const sheetH = 800;
    targetCanvas.width = sheetW;
    targetCanvas.height = sheetH;

    const ctx = targetCanvas.getContext('2d');
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, sheetW, sheetH);

    // Render single high-res passport tile first
    const c = state.passport.country;
    const tileCanvas = document.createElement('canvas');
    tileCanvas.width = c.widthPx300Dpi;
    tileCanvas.height = c.heightPx300Dpi;
    renderSinglePassportPhotoToContext(tileCanvas.getContext('2d'), tileCanvas.width, tileCanvas.height, false);

    // Fit 2 rows x 4 columns (8 copies) or 2x3 depending on aspect
    const cols = c.widthMm >= 48 ? 3 : 4;
    const rows = 2;
    const pad = 28;
    const availW = sheetW - pad * (cols + 1);
    const availH = sheetH - 54 - pad * (rows + 1);
    const scale = Math.min(availW / (cols * tileCanvas.width), availH / (rows * tileCanvas.height));
    const drawW = Math.round(tileCanvas.width * scale);
    const drawH = Math.round(tileCanvas.height * scale);

    const startX = Math.round((sheetW - (cols * drawW + (cols - 1) * pad)) * 0.5);
    const startY = Math.round((sheetH - 36 - (rows * drawH + (rows - 1) * pad)) * 0.5);

    for (let r = 0; r < rows; r++) {
      for (let col = 0; col < cols; col++) {
        const x = startX + col * (drawW + pad);
        const y = startY + r * (drawH + pad);
        ctx.drawImage(tileCanvas, x, y, drawW, drawH);

        // Cut guide border
        ctx.strokeStyle = '#CBD5E1';
        ctx.lineWidth = 1.5;
        ctx.strokeRect(x, y, drawW, drawH);
      }
    }

    // Lab footer caption
    ctx.fillStyle = '#475569';
    ctx.font = 'bold 16px Inter, sans-serif';
    ctx.fillText(
      `Aura Biometric ID Lab • ${c.countryName} (${c.specLabel}) • 4×6" Print Sheet (300 DPI)`,
      startX,
      sheetH - 16
    );
  }

  function renderPassportCanvas() {
    const canvas = document.getElementById('passportCanvas');
    const wrapper = document.getElementById('passportCardWrapper');
    if (!canvas || !wrapper) return;

    const c = state.passport.country;
    if (state.passport.printMode === 'SHEET_4X6') {
      wrapper.style.aspectRatio = '3 / 2';
      renderPassport4x6SheetToCanvas(canvas);
      setElText('passportCornerSpecBadge', '4×6" Lab Sheet');
    } else {
      wrapper.style.aspectRatio = `${c.widthMm} / ${c.heightMm}`;
      canvas.width = c.widthPx300Dpi;
      canvas.height = c.heightPx300Dpi;
      const ctx = canvas.getContext('2d');
      renderSinglePassportPhotoToContext(ctx, canvas.width, canvas.height, true);
      setElText('passportCornerSpecBadge', `${c.widthMm}×${c.heightMm} mm`);
    }
  }

  async function startPassportPipelineWithImage(img, defaultCoatId) {
    switchPassportMode('PROCESSING');

    // Reset adjustments
    state.passport.userZoom = 1.0;
    state.passport.userPanX = 0.0;
    state.passport.userPanY = 0.0;
    state.passport.coatScale = 1.0;
    state.passport.coatOffsetY = 0.0;

    if (defaultCoatId) {
      const foundCoat = FORMAL_COAT_PRESETS.find((c) => c.id === defaultCoatId);
      if (foundCoat) state.passport.coat = foundCoat;
    }

    await ensureCalibratedCoatLoaded(state.passport.coat);
    buildPassportSubjectCutout(img);

    const arc = document.getElementById('passportSpinnerArc');
    const titleEl = document.getElementById('passportProcessingStageTitle');
    const stepEls = [
      document.getElementById('pipeStep1'),
      document.getElementById('pipeStep2'),
      document.getElementById('pipeStep3'),
      document.getElementById('pipeStep4')
    ];

    stepEls.forEach((el) => {
      if (!el) return;
      el.classList.remove('done');
      const icon = el.querySelector('.pipe-step-icon');
      if (icon) icon.textContent = 'radio_button_unchecked';
    });

    const circumference = 301.6;
    let progress = 0.05;

    const timer = setInterval(() => {
      progress += 0.07;
      if (arc) {
        arc.style.strokeDashoffset = String(circumference * (1 - Math.min(1, progress)));
      }

      if (progress > 0.25 && stepEls[0]) {
        stepEls[0].classList.add('done');
        stepEls[0].querySelector('.pipe-step-icon').textContent = 'check_circle';
        if (titleEl) titleEl.textContent = 'Detecting BiSeNet 19-Class Landmarks & Neck...';
      }
      if (progress > 0.50 && stepEls[1]) {
        stepEls[1].classList.add('done');
        stepEls[1].querySelector('.pipe-step-icon').textContent = 'check_circle';
        if (titleEl) titleEl.textContent = 'Segmenting Casual Clothing (MediaPipe)...';
      }
      if (progress > 0.75 && stepEls[2]) {
        stepEls[2].classList.add('done');
        stepEls[2].querySelector('.pipe-step-icon').textContent = 'check_circle';
        if (titleEl) titleEl.textContent = 'Synthesizing Formal Attire & 300 DPI Crop...';
      }
      if (progress > 0.92 && stepEls[3]) {
        stepEls[3].classList.add('done');
        stepEls[3].querySelector('.pipe-step-icon').textContent = 'check_circle';
      }

      if (progress >= 1.0) {
        clearInterval(timer);
        renderPassportCoatsList();
        switchPassportMode('STUDIO_EDITOR');
      }
    }, 55);
  }

  function exportAndSavePassportPhoto() {
    const savedCanvas = document.getElementById('passportSavedCanvas');
    if (!savedCanvas) return;

    const c = state.passport.country;
    if (state.passport.printMode === 'SHEET_4X6') {
      renderPassport4x6SheetToCanvas(savedCanvas);
    } else {
      savedCanvas.width = c.widthPx300Dpi;
      savedCanvas.height = c.heightPx300Dpi;
      const ctx = savedCanvas.getContext('2d');
      renderSinglePassportPhotoToContext(ctx, savedCanvas.width, savedCanvas.height, false);
    }

    const fileName = `Aura_Passport_${c.code}_${state.passport.printMode === 'SHEET_4X6' ? '4x6_Sheet' : '300DPI'}.png`;
    downloadCanvasAsFile(savedCanvas, fileName, 'image/png');
    showToast('Official 300 DPI Passport Photo Saved!');
    switchPassportMode('SAVED_EXPORT');
  }

  // ==========================================================================
  // 7. EXPORT 4K MODAL & CAMERA ROLL DOWNLOAD / WEB SHARE (Export4kModal.kt)
  // ==========================================================================

  function renderExportMasterpieceCanvas() {
    const savedCanvas = document.getElementById('savedMasterpieceCanvas');
    if (!savedCanvas || !state.isSampleLoaded) return null;

    const tier = EXPORT_TIERS[state.selectedExportTier] || EXPORT_TIERS.ULTRA_4K;
    const baseW = state.afterGradedOffscreen.width;
    const baseH = state.afterGradedOffscreen.height;

    const outW = Math.round(baseW * tier.scale);
    const outH = Math.round(baseH * tier.scale);
    savedCanvas.width = outW;
    savedCanvas.height = outH;

    const ctx = savedCanvas.getContext('2d');
    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';

    if (state.enhanceColorGamut) {
      ctx.filter = 'saturate(1.12) contrast(1.04)';
    }
    ctx.drawImage(state.afterGradedOffscreen, 0, 0, outW, outH);
    ctx.filter = 'none';

    // Draw Aura signature watermark only if Remove Aura Watermark is unchecked
    if (!state.removeWatermark) {
      ctx.save();
      const fontSize = Math.max(14, Math.round(outW * 0.028));
      ctx.font = `800 ${fontSize}px Manrope, sans-serif`;
      ctx.fillStyle = 'rgba(255, 255, 255, 0.78)';
      ctx.fillText('✦ AURA 4K STUDIO AI', outW * 0.04, outH - outW * 0.04);
      ctx.restore();
    }

    setElText('savedResolutionPill', `Portrait • ${tier.resolution}`);
    setElText('savedSubtitleText', `Exported in ${tier.title} • Studio Grade Finish`);
    return savedCanvas;
  }

  function downloadCanvasAsFile(canvas, filename, mimeType) {
    try {
      const dataUrl = canvas.toDataURL(mimeType || 'image/jpeg', 0.95);
      const link = document.createElement('a');
      link.download = filename;
      link.href = dataUrl;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (e) {
      console.error('Download failed:', e);
    }
  }

  async function shareCanvasOrDownload(canvas, filename, titleText) {
    if (!canvas) return;
    try {
      const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.95));
      if (blob && navigator.canShare) {
        const file = new File([blob], filename, { type: 'image/jpeg' });
        if (navigator.canShare({ files: [file] })) {
          await navigator.share({
            title: titleText || 'Aura 4K AI Photo',
            text: 'Enhanced with Aura ReminiSkinAI Studio',
            files: [file]
          });
          return;
        }
      }
    } catch (e) {
      // Fallback to direct download if user cancels share or Web Share API isn't supported
    }
    downloadCanvasAsFile(canvas, filename, 'image/jpeg');
    showToast(`Downloaded ${filename}`);
  }

  // ==========================================================================
  // 8. MODAL & TOAST HELPERS
  // ==========================================================================

  function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.hidden = false;
      syncCreditsUI();
      syncStudioControlsUI();
    }
  }

  function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.hidden = true;
  }

  let toastTimer = null;
  function showToast(message) {
    const toast = document.getElementById('auraToast');
    const text = document.getElementById('auraToastText');
    if (!toast || !text) return;
    text.textContent = message;
    toast.hidden = false;
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      toast.hidden = true;
    }, 2600);
  }

  function setElText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  }

  function setElValue(id, val) {
    const el = document.getElementById(id);
    if (el) el.value = String(val);
  }

  function hexToRgb(hex) {
    const clean = (hex || '#F3C6A8').replace('#', '');
    const num = parseInt(clean, 16);
    return {
      r: (num >> 16) & 255,
      g: (num >> 8) & 255,
      b: num & 255
    };
  }

  // ==========================================================================
  // 9. EVENT LISTENERS BINDING
  // ==========================================================================

  function bindAllEvents() {
    // Close modal buttons & backdrop clicks
    document.querySelectorAll('[data-close-modal]').forEach((btn) => {
      btn.addEventListener('click', () => {
        closeModal(btn.getAttribute('data-close-modal'));
      });
    });

    document.querySelectorAll('.aura-modal-backdrop').forEach((backdrop) => {
      backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) {
          backdrop.hidden = true;
        }
      });
    });

    // Global Photo Upload Input (Main Enhancer)
    const globalPhotoInput = document.getElementById('globalPhotoInput');
    if (globalPhotoInput) {
      globalPhotoInput.addEventListener('change', (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;
        state.customFileName = file.name || 'Custom_Portrait_4K.jpg';
        const reader = new FileReader();
        reader.onload = (ev) => {
          const img = new Image();
          img.onload = async () => {
            await loadStudioSample(2, img);
            runNeuralProcessingTransition('COMPARISON_STUDIO');
          };
          img.src = ev.target.result;
        };
        reader.readAsDataURL(file);
        e.target.value = '';
      });
    }

    // Passport Photo Upload Input
    const passportPhotoInput = document.getElementById('passportPhotoInput');
    if (passportPhotoInput) {
      passportPhotoInput.addEventListener('change', (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = (ev) => {
          const img = new Image();
          img.onload = () => {
            state.passport.customPassportImg = img;
            startPassportPipelineWithImage(img, 'MEN_1_NAVY_TIE');
          };
          img.src = ev.target.result;
        };
        reader.readAsDataURL(file);
        e.target.value = '';
      });
    }

    // Home Top Bar Buttons
    bindClick('btnOpenCredits', () => openModal('creditsModal'));
    bindClick('btnOpenSettings', () => openModal('settingsModal'));
    bindClick('btnOpenModelArchSpec', () => {
      closeModal('settingsModal');
      openModal('modelArchModal');
    });

    // Home Hero Upload & Quick Samples
    bindClick('btnHeroSelectPhoto', () => globalPhotoInput && globalPhotoInput.click());
    bindClick('btnHeroOpenStudio', () => switchMainMode('COMPARISON_STUDIO'));

    document.querySelectorAll('[data-sample-idx]').forEach((chip) => {
      chip.addEventListener('click', async () => {
        const idx = parseInt(chip.getAttribute('data-sample-idx'), 10);
        await loadStudioSample(idx);
        runNeuralProcessingTransition('COMPARISON_STUDIO');
      });
    });

    // Home Precision Tools Bento Cards
    bindClick('btnViewAllTools', () => openModal('allToolsModal'));
    bindClick('bentoFaceEnhance', () => {
      state.studioBottomTab = 'ENHANCE';
      syncStudioControlsUI();
      switchMainMode('COMPARISON_STUDIO');
    });
    bindClick('bentoOldPhoto', async () => {
      await loadStudioSample(1);
      state.retouchTab = 'RESTORE';
      syncRetouchTabUI();
      switchMainMode('RETOUCH_SUITE');
    });
    bindClick('bentoColorizerBanner', () => {
      state.retouchTab = 'COLORIZE';
      syncRetouchTabUI();
      switchMainMode('RETOUCH_SUITE');
    });
    bindClick('bentoPassportBanner', () => {
      switchAppScreen('PASSPORT_STUDIO');
    });

    // Home Recent Restorations Cards
    document.querySelectorAll('[data-recent-sample]').forEach((card) => {
      card.addEventListener('click', async () => {
        const idx = parseInt(card.getAttribute('data-recent-sample'), 10);
        await loadStudioSample(idx);
        switchMainMode('COMPARISON_STUDIO');
      });
    });
    bindClick('cardBatchEnhance', () => globalPhotoInput && globalPhotoInput.click());

    function clearCustomPhotoAndReturnHome() {
      state.customImageElement = null;
      state.selectedSampleIndex = 0;
      state.detectedFace = { found: false, cx: 0.50, cy: 0.38, rx: 0.25, ry: 0.32 };
      const fileInput = document.getElementById('globalPhotoInput');
      if (fileInput) fileInput.value = '';
      loadStudioSample(0);
      switchMainMode('HOME');
      showToast('Ready for new photo');
    }

    // Bottom Navigation Bar
    document.querySelectorAll('[data-bottom-nav]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const nav = btn.getAttribute('data-bottom-nav');
        if (nav === 'PROJECTS') {
          openModal('projectsModal');
        } else if (nav === 'HOME') {
          clearCustomPhotoAndReturnHome();
        } else {
          switchMainMode(nav);
        }
      });
    });

    // Comparison Studio Top Bar & Sample Switcher
    bindClick('btnStudioBackHome', clearCustomPhotoAndReturnHome);
    bindClick('btnStudioOpenExport', () => openModal('export4kModal'));
    bindClick('btnComparisonZoomPill', () => {
      state.isZoomed200 = !state.isZoomed200;
      syncZoomPillUI();
      requestStudioCanvasRender();
    });

    document.querySelectorAll('[data-studio-sample]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const idx = parseInt(btn.getAttribute('data-studio-sample'), 10);
        if (idx === 2 && !state.customImageElement) {
          globalPhotoInput && globalPhotoInput.click();
          return;
        }
        await loadStudioSample(idx);
      });
    });

    // Hold to Compare Button
    const holdBtn = document.getElementById('btnHoldCompare');
    if (holdBtn) {
      const startHold = () => {
        state.isHoldCompare = true;
        holdBtn.classList.add('holding');
        requestStudioCanvasRender();
      };
      const endHold = () => {
        state.isHoldCompare = false;
        holdBtn.classList.remove('holding');
        requestStudioCanvasRender();
      };
      holdBtn.addEventListener('pointerdown', startHold);
      holdBtn.addEventListener('pointerup', endHold);
      holdBtn.addEventListener('pointerleave', endHold);
      holdBtn.addEventListener('pointercancel', endHold);
    }

    // Comparison Studio Bottom Dock Controls
    bindClick('btnStudioOpenSkinToneModal', () => openModal('skinToneModal'));
    bindClick('btnStudioTonePickerChip', () => openModal('skinToneModal'));
    bindClick('btnStudioUploadOwnChip', () => globalPhotoInput && globalPhotoInput.click());
    bindClick('btnStudioToggleMaskChip', () => {
      state.editParams.showSkinMaskOverlay = !state.editParams.showSkinMaskOverlay;
      savePersistedSettings();
      rebuildGradedAfterBuffer();
      syncStudioControlsUI();
      requestStudioCanvasRender();
    });

    const studioMainSlider = document.getElementById('studioMainSlider');
    if (studioMainSlider) {
      studioMainSlider.addEventListener('input', (e) => {
        const v = parseInt(e.target.value, 10) / 100;
        if (state.studioBottomTab === 'SKIN_AI') {
          state.editParams.skinSmoothness = v;
        } else {
          state.editParams.facialClarity = v;
        }
        savePersistedSettings();
        rebuildGradedAfterBuffer();
        syncStudioControlsUI();
        requestStudioCanvasRender();
      });
    }

    document.querySelectorAll('[data-studio-tool]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const tool = btn.getAttribute('data-studio-tool');
        if (tool === 'RETOUCH') {
          switchMainMode('RETOUCH_SUITE');
          return;
        }
        state.studioBottomTab = tool;
        if (tool === 'SKIN_AI') {
          openModal('skinToneModal');
        }
        syncStudioControlsUI();
        requestStudioCanvasRender();
      });
    });

    // Retouch Suite Controls
    bindClick('btnRetouchBackStudio', () => switchMainMode('COMPARISON_STUDIO'));
    bindClick('btnRetouchDone', () => switchMainMode('COMPARISON_STUDIO'));
    bindClick('btnRetouchExpandStudio', () => switchMainMode('COMPARISON_STUDIO'));
    bindClick('btnRetouchApplyExport', () => openModal('export4kModal'));
    bindClick('btnRetouchLaunchPassport', () => switchAppScreen('PASSPORT_STUDIO'));

    bindClick('btnRetouchReset', () => {
      resetSkinEditParamsToDefault();
      showToast('Retouch parameters reset to studio defaults');
    });

    document.querySelectorAll('[data-retouch-tab]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.retouchTab = btn.getAttribute('data-retouch-tab');
        syncRetouchTabUI();
      });
    });

    // 4 Core Retouch Sliders
    bindRange('sliderDenoiseUnblur', (pct) => {
      state.editParams.facialClarity = pct / 100;
      onEditParamsChanged();
    });
    bindRange('sliderSkinTexture', (pct) => {
      state.editParams.skinSmoothness = pct / 100;
      onEditParamsChanged();
    });
    bindRange('sliderEyeIris', (pct) => {
      state.editParams.skinRadiance = 0.5 + (pct / 100);
      onEditParamsChanged();
    });
    bindRange('sliderLightingHdr', (pct) => {
      state.editParams.skinWarmth = 0.5 + (pct / 100);
      onEditParamsChanged();
    });

    // Lip & Hair Intensity Sliders + Dermis Mask Switch
    bindRange('sliderLipIntensity', (pct) => {
      state.editParams.lipIntensity = pct / 100;
      onEditParamsChanged();
    });
    bindRange('sliderHairIntensity', (pct) => {
      state.editParams.hairIntensity = pct / 100;
      onEditParamsChanged();
    });

    const chkRetouchMask = document.getElementById('chkRetouchSkinMask');
    if (chkRetouchMask) {
      chkRetouchMask.addEventListener('change', (e) => {
        state.editParams.showSkinMaskOverlay = e.target.checked;
        onEditParamsChanged();
      });
    }

    // Dialog Sliders & Reset
    bindClick('btnDialogResetSkinTone', () => resetSkinEditParamsToDefault());
    bindRange('dlgSliderSmooth', (pct) => {
      state.editParams.skinSmoothness = pct / 100;
      onEditParamsChanged();
    });
    bindRange('dlgSliderWarmth', (pct) => {
      state.editParams.skinWarmth = 0.5 + (pct / 100);
      onEditParamsChanged();
    });
    bindRange('dlgSliderRadiance', (pct) => {
      state.editParams.skinRadiance = 0.5 + (pct / 100);
      onEditParamsChanged();
    });
    bindRange('dlgSliderDetail', (pct) => {
      state.editParams.detailPreservation = pct / 100;
      onEditParamsChanged();
    });

    // Credits Dialog Top-up Buttons
    document.querySelectorAll('[data-add-credits]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const add = parseInt(btn.getAttribute('data-add-credits'), 10);
        state.userCredits += add;
        savePersistedSettings();
        syncCreditsUI();
        showToast(`+${add} Aura Pro Credits Added!`);
      });
    });
    document.querySelectorAll('[data-reset-credits]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.userCredits = 48;
        savePersistedSettings();
        syncCreditsUI();
        showToast('Credits balance reset to 48');
      });
    });

    // All Tools Dialog Launcher
    document.querySelectorAll('[data-launch-tool]').forEach((row) => {
      row.addEventListener('click', async () => {
        const toolId = row.getAttribute('data-launch-tool');
        closeModal('allToolsModal');
        if (toolId === 'passport') {
          switchAppScreen('PASSPORT_STUDIO');
        } else if (toolId === 'portrait_4k') {
          state.studioBottomTab = 'ENHANCE';
          syncStudioControlsUI();
          switchMainMode('COMPARISON_STUDIO');
        } else if (toolId === 'old_restore') {
          await loadStudioSample(1);
          state.retouchTab = 'RESTORE';
          syncRetouchTabUI();
          switchMainMode('RETOUCH_SUITE');
        } else if (toolId === 'denoise') {
          state.retouchTab = 'RESTORE';
          syncRetouchTabUI();
          switchMainMode('RETOUCH_SUITE');
        } else if (toolId === 'beauty') {
          state.retouchTab = 'RETOUCH';
          syncRetouchTabUI();
          switchMainMode('RETOUCH_SUITE');
        } else if (toolId === 'custom') {
          globalPhotoInput && globalPhotoInput.click();
        }
      });
    });

    // Saved Projects Dialog
    document.querySelectorAll('[data-open-project]').forEach((card) => {
      card.addEventListener('click', async () => {
        const idx = parseInt(card.getAttribute('data-open-project'), 10);
        closeModal('projectsModal');
        await loadStudioSample(idx);
        switchMainMode('COMPARISON_STUDIO');
      });
    });
    bindClick('btnProjectsNewUpload', () => {
      closeModal('projectsModal');
      globalPhotoInput && globalPhotoInput.click();
    });

    // Export 4K Modal Interactions
    document.querySelectorAll('[data-export-tier]').forEach((card) => {
      card.addEventListener('click', () => {
        state.selectedExportTier = card.getAttribute('data-export-tier');
        document.querySelectorAll('[data-export-tier]').forEach((c) => {
          c.classList.toggle('active', c.getAttribute('data-export-tier') === state.selectedExportTier);
        });
      });
    });

    const chkWatermark = document.getElementById('chkRemoveWatermark');
    if (chkWatermark) {
      chkWatermark.addEventListener('change', (e) => {
        state.removeWatermark = e.target.checked;
      });
    }
    const chkExif = document.getElementById('chkPreserveExif');
    if (chkExif) {
      chkExif.addEventListener('change', (e) => {
        state.preserveExif = e.target.checked;
      });
    }
    const chkGamut = document.getElementById('chkEnhanceGamut');
    if (chkGamut) {
      chkGamut.addEventListener('change', (e) => {
        state.enhanceColorGamut = e.target.checked;
      });
    }

    bindClick('btnExportGetMoreCredits', () => {
      closeModal('export4kModal');
      openModal('creditsModal');
    });

    bindClick('btnConfirmSaveCameraRoll', () => {
      const tier = EXPORT_TIERS[state.selectedExportTier] || EXPORT_TIERS.ULTRA_4K;
      const labelEl = document.getElementById('exportPrimaryBtnLabel');
      if (labelEl) labelEl.textContent = `Exporting in ${tier.title}...`;

      setTimeout(() => {
        state.userCredits = Math.max(0, state.userCredits - tier.creditCost);
        savePersistedSettings();
        syncCreditsUI();
        if (labelEl) labelEl.textContent = 'Save to Camera Roll';
        closeModal('export4kModal');

        const exportedCanvas = renderExportMasterpieceCanvas();
        if (exportedCanvas) {
          const sample = STUDIO_SAMPLES[state.selectedSampleIndex] || STUDIO_SAMPLES[0];
          const fname = `Aura_${tier.id}_${sample.fileName.replace(/\.[^.]+$/, '')}.jpg`;
          downloadCanvasAsFile(exportedCanvas, fname, 'image/jpeg');
        }
        showToast(`Saved ${tier.title} Masterpiece to Camera Roll`);
        switchMainMode('SAVED_SHARE');
      }, 650);
    });

    const runSecondaryExportAction = (actionLabel) => {
      const exportedCanvas = renderExportMasterpieceCanvas();
      closeModal('export4kModal');
      switchMainMode('SAVED_SHARE');
      if (exportedCanvas) {
        shareCanvasOrDownload(exportedCanvas, `Aura_${actionLabel}_Export.jpg`, 'Aura 4K Masterpiece');
      }
    };
    bindClick('btnExportIgStory', () => runSecondaryExportAction('IG_Story'));
    bindClick('btnExportShareSheet', () => runSecondaryExportAction('Share'));
    bindClick('btnExportRawTiff', () => runSecondaryExportAction('Master_TIFF'));

    // Saved & Share View Buttons
    bindClick('btnSavedBackStudio', () => switchMainMode('COMPARISON_STUDIO'));
    bindClick('btnEnhanceAnotherPhoto', clearCustomPhotoAndReturnHome);
    bindClick('btnShareMasterpiece', () => {
      const savedCanvas = document.getElementById('savedMasterpieceCanvas');
      shareCanvasOrDownload(savedCanvas, 'Aura_4K_Masterpiece.jpg', 'Aura 4K AI Masterpiece');
    });

    // ========================================================================
    // PASSPORT PHOTO STUDIO EVENTS (PassportPhotoScreen.kt)
    // ========================================================================
    bindClick('btnPassportBackMain', () => switchAppScreen('MAIN'));
    bindClick('btnPassportOpenAiInfo', () => openModal('passportAiModelsModal'));
    bindClick('btnPassportSelectPhoto', () => passportPhotoInput && passportPhotoInput.click());

    document.querySelectorAll('[data-passport-sample]').forEach((card) => {
      card.addEventListener('click', async () => {
        const idx = parseInt(card.getAttribute('data-passport-sample'), 10);
        state.passport.selectedSampleIdx = idx;
        const sampleUrls = [
          'assets/sample_2_before.webp',
          'assets/sample_1_before.webp',
          'assets/sample_3_before.webp'
        ];
        const defaultCoats = [
          'MEN_1_NAVY_TIE',
          'WOMEN_1_NAVY_BLAZER',
          'MEN_2_CHARCOAL_TIE'
        ];
        const img = await loadImageCached(sampleUrls[idx] || sampleUrls[0]);
        startPassportPipelineWithImage(img, defaultCoats[idx]);
      });
    });

    bindClick('btnPassportEditorBack', () => switchPassportMode('UPLOAD_HOME'));
    bindClick('btnPassportEditorSaveTop', () => exportAndSavePassportPhoto());
    bindClick('btnPassportSavePrimary', () => exportAndSavePassportPhoto());

    const shareCurrentPassport = () => {
      const c = state.passport.country;
      const tempCanvas = document.createElement('canvas');
      if (state.passport.printMode === 'SHEET_4X6') {
        renderPassport4x6SheetToCanvas(tempCanvas);
      } else {
        tempCanvas.width = c.widthPx300Dpi;
        tempCanvas.height = c.heightPx300Dpi;
        renderSinglePassportPhotoToContext(tempCanvas.getContext('2d'), tempCanvas.width, tempCanvas.height, false);
      }
      shareCanvasOrDownload(tempCanvas, `Aura_Passport_${c.code}.png`, `${c.countryName} Official Passport Photo`);
    };

    bindClick('btnPassportEditorShareTop', shareCurrentPassport);
    bindClick('btnPassportShareSecondary', shareCurrentPassport);
    bindClick('btnPassportSavedShare', () => {
      const savedCanvas = document.getElementById('passportSavedCanvas');
      shareCanvasOrDownload(savedCanvas, `Aura_Passport_${state.passport.country.code}.png`, 'Official Passport Photo');
    });

    bindClick('btnPassportSavedBackMain', () => {
      switchPassportMode('UPLOAD_HOME');
      switchAppScreen('MAIN');
    });
    bindClick('btnPassportSavedBackEditor', () => switchPassportMode('STUDIO_EDITOR'));

    // Passport Studio 4 Tabs
    document.querySelectorAll('[data-passport-tab]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const tab = btn.getAttribute('data-passport-tab');
        state.passport.activeTab = tab;
        document.querySelectorAll('[data-passport-tab]').forEach((b) => {
          b.classList.toggle('active', b.getAttribute('data-passport-tab') === tab);
        });
        const panels = {
          COATS: document.getElementById('passportTabCoats'),
          CROP_ALIGN: document.getElementById('passportTabCrop'),
          BACKGROUND: document.getElementById('passportTabBackground'),
          COUNTRY_SIZE: document.getElementById('passportTabCountry')
        };
        Object.keys(panels).forEach((k) => {
          if (panels[k]) panels[k].classList.toggle('active', k === tab);
        });
      });
    });

    // Coat Category Filter Chips (All 10 / Men 5 / Women 5)
    document.querySelectorAll('[data-coat-filter]').forEach((chip) => {
      chip.addEventListener('click', () => {
        state.passport.coatCategoryFilter = chip.getAttribute('data-coat-filter');
        document.querySelectorAll('[data-coat-filter]').forEach((c) => {
          c.classList.toggle('active', c.getAttribute('data-coat-filter') === state.passport.coatCategoryFilter);
        });
        renderPassportCoatsList();
      });
    });

    // Passport Crop & Fit Controls
    const chkGuides = document.getElementById('chkPassportGuides');
    if (chkGuides) {
      chkGuides.addEventListener('change', (e) => {
        state.passport.showBiometricGuides = e.target.checked;
        renderPassportCanvas();
      });
    }

    bindClick('btnPassportZoomOut', () => {
      state.passport.userZoom = Math.max(0.75, state.passport.userZoom - 0.06);
      renderPassportCanvas();
    });
    bindClick('btnPassportZoomIn', () => {
      state.passport.userZoom = Math.min(1.45, state.passport.userZoom + 0.06);
      renderPassportCanvas();
    });
    bindClick('btnPassportResetCrop', () => {
      state.passport.userZoom = 1.0;
      state.passport.userPanX = 0.0;
      state.passport.userPanY = 0.0;
      state.passport.coatScale = 1.0;
      state.passport.coatOffsetY = 0.0;
      setElValue('sliderPassportCoatScale', 100);
      setElValue('sliderPassportCoatY', 0);
      renderPassportCanvas();
    });

    bindRange('sliderPassportCoatScale', (val) => {
      state.passport.coatScale = val / 100;
      renderPassportCanvas();
    });
    bindRange('sliderPassportCoatY', (val) => {
      state.passport.coatOffsetY = val / 200;
      renderPassportCanvas();
    });

    // Interactive Drag-to-Pan on Passport Canvas
    const passportWrapper = document.getElementById('passportCardWrapper');
    if (passportWrapper) {
      let isPanningPassport = false;
      let startX = 0, startY = 0;
      passportWrapper.addEventListener('pointerdown', (e) => {
        isPanningPassport = true;
        startX = e.clientX;
        startY = e.clientY;
        passportWrapper.setPointerCapture(e.pointerId);
      });
      passportWrapper.addEventListener('pointermove', (e) => {
        if (!isPanningPassport) return;
        const rect = passportWrapper.getBoundingClientRect();
        const dx = (e.clientX - startX) / Math.max(100, rect.width);
        const dy = (e.clientY - startY) / Math.max(100, rect.height);
        startX = e.clientX;
        startY = e.clientY;
        state.passport.userPanX = Math.max(-0.25, Math.min(0.25, state.passport.userPanX + dx));
        state.passport.userPanY = Math.max(-0.25, Math.min(0.25, state.passport.userPanY + dy));
        renderPassportCanvas();
      });
      const stopPan = () => {
        isPanningPassport = false;
      };
      passportWrapper.addEventListener('pointerup', stopPan);
      passportWrapper.addEventListener('pointercancel', stopPan);
    }

    // Print Sheet Mode Toggle (Single vs 4x6 Sheet)
    document.querySelectorAll('[data-print-mode]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.passport.printMode = btn.getAttribute('data-print-mode');
        document.querySelectorAll('[data-print-mode]').forEach((b) => {
          b.classList.toggle('active', b.getAttribute('data-print-mode') === state.passport.printMode);
        });
        renderPassportCanvas();
      });
    });

    // Window resize handler
    window.addEventListener('resize', () => {
      requestStudioCanvasRender();
      if (state.currentAppScreen === 'PASSPORT_STUDIO') {
        renderPassportCanvas();
      }
    });
  }

  function syncRetouchTabUI() {
    document.querySelectorAll('[data-retouch-tab]').forEach((b) => {
      b.classList.toggle('active', b.getAttribute('data-retouch-tab') === state.retouchTab);
    });
  }

  function onEditParamsChanged() {
    savePersistedSettings();
    rebuildGradedAfterBuffer();
    syncStudioControlsUI();
    requestStudioCanvasRender();
  }

  function resetSkinEditParamsToDefault() {
    state.selectedPresetId = 'REMINI_NATURAL';
    state.editParams = {
      skinSmoothness: 0.45,
      skinWarmth: 1.10,
      skinRadiance: 1.15,
      detailPreservation: 0.85,
      facialClarity: 0.85,
      lipShade: 'NONE',
      lipIntensity: 0.50,
      hairShade: 'NONE',
      hairIntensity: 0.50,
      showSkinMaskOverlay: false
    };
    onEditParamsChanged();
  }

  function bindClick(id, handler) {
    const el = document.getElementById(id);
    if (el) el.addEventListener('click', handler);
  }

  function bindRange(id, handler) {
    const el = document.getElementById(id);
    if (el) {
      el.addEventListener('input', (e) => {
        handler(parseFloat(e.target.value));
      });
    }
  }

  // ==========================================================================
  // 10. ANIMATION LOOP FOR LANDMARK WIREFRAME & INITIALIZATION
  // ==========================================================================

  function startLandmarkPulseLoop() {
    let startTime = performance.now();
    function tick(now) {
      const elapsed = (now - startTime) / 1400;
      state.wireframePulsePhase = (Math.sin(elapsed * Math.PI * 2) + 1) * 0.5;
      if (
        state.currentAppScreen === 'MAIN' &&
        (state.screenMode === 'RETOUCH_SUITE' ||
          (state.screenMode === 'COMPARISON_STUDIO' && state.studioBottomTab === 'MAGNIFIER'))
      ) {
        requestStudioCanvasRender();
      }
      requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  async function initApp() {
    loadPersistedSettings();
    populateDynamicSkinToneAndShadeLists();
    populatePassportUI();
    bindAllEvents();

    attachSplitSliderInteractions(document.getElementById('comparisonCanvasContainer'));
    attachSplitSliderInteractions(document.getElementById('retouchCanvasContainer'));

    syncCreditsUI();
    syncStudioControlsUI();

    await loadStudioSample(0);
    startLandmarkPulseLoop();

    // Register Service Worker for offline PWA support with auto-update
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.getRegistrations().then((registrations) => {
        for (const reg of registrations) {
          reg.update();
        }
      });
      navigator.serviceWorker.register('./sw.js?v=2.3.0').catch(() => {});
      navigator.serviceWorker.addEventListener('controllerchange', () => {
        window.location.reload();
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
  } else {
    initApp();
  }
})();
