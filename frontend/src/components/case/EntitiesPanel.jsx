import IntelligencePanel from "./IntelligencePanel";

export default function EntitiesPanel({ caseId }) {
  return (
    <IntelligencePanel
      caseId={caseId}
      view="entities"
    />
  );
}