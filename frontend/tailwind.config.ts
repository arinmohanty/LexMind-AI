import type { Config } from "tailwindcss";

/**
 * Maps the LexMind design-system tokens (CSS variables in index.css) to Tailwind colors,
 * so design and code share one source of truth (Phase 3 / 01-design-system).
 */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    container: { center: true, padding: "1.5rem", screens: { "2xl": "1440px" } },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        brand: { DEFAULT: "hsl(var(--brand))", foreground: "hsl(var(--brand-fg))" },
        accent: { DEFAULT: "hsl(var(--accent))", foreground: "hsl(var(--accent-fg))" },
        muted: { DEFAULT: "hsl(var(--muted))", foreground: "hsl(var(--muted-foreground))" },
        card: { DEFAULT: "hsl(var(--card))", foreground: "hsl(var(--foreground))" },
        success: "hsl(var(--success))",
        warning: "hsl(var(--warning))",
        danger: "hsl(var(--danger))",
        info: "hsl(var(--info))",
      },
      borderRadius: { lg: "12px", md: "8px", sm: "6px" },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
        serif: ["Source Serif 4", "Lora", "ui-serif", "Georgia", "serif"],
        mono: ["JetBrains Mono", "ui-monospace", "monospace"],
      },
      keyframes: {
        "accordion-down": { from: { height: "0" }, to: { height: "var(--radix-accordion-content-height)" } },
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
} satisfies Config;
