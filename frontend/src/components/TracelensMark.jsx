export default function TraceLensMark() {
  return (
    <svg
      className="trace-mark"
      viewBox="0 0 64 64"
      role="img"
      aria-label="TraceLens AI"
    >
      <rect
        x="7"
        y="7"
        width="50"
        height="50"
        rx="2"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      />

      <circle
        cx="30"
        cy="29"
        r="13"
        fill="none"
        stroke="currentColor"
        strokeWidth="3"
      />

      <path
        d="M39.5 38.5L51 50"
        fill="none"
        stroke="currentColor"
        strokeWidth="4"
        strokeLinecap="square"
      />

      <path
        d="M30 11V18M30 40V47M12 29H19M41 29H48"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      />

      <circle cx="30" cy="29" r="2.5" fill="currentColor" />
    </svg>
  );
}