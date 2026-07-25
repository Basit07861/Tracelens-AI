import IntelligencePanel from "./IntelligencePanel";

export default function TimelinePanel({ caseId }) {
  return (
    <IntelligencePanel
      caseId={caseId}
      view="timeline"
    />
  );
}