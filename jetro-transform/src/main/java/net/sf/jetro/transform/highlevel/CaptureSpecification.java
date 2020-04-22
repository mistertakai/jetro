package net.sf.jetro.transform.highlevel;

import java.util.Objects;
import java.util.function.Function;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.JsonArray;
import net.sf.jetro.tree.JsonBoolean;
import net.sf.jetro.tree.JsonNumber;
import net.sf.jetro.tree.JsonString;
import net.sf.jetro.tree.JsonType;
import net.sf.jetro.tree.visitor.JsonTreeBuildingVisitor;
import net.sf.jetro.visitor.JsonArrayVisitor;
import net.sf.jetro.visitor.JsonObjectVisitor;
import net.sf.jetro.visitor.JsonVisitor;
import net.sf.jetro.visitor.chained.MultiplexingJsonVisitor;
import net.sf.jetro.visitor.pathaware.PathAwareJsonVisitor;

public class CaptureSpecification {
	private static final String EDITOR_NOT_NULL = "editor must not be null";
	
	private final JsonPath path;
	private final TransformationSpecification specification;
	
	CaptureSpecification(final JsonPath path,
			final TransformationSpecification specification) {
		Objects.requireNonNull(path, "path must not be null");
		Objects.requireNonNull(specification, "specification must not be null");
		
		this.path = path;
		this.specification = specification;
	}

	public <S extends JsonType, T extends JsonType> CaptureEditSpecification<S, T>
	edit(final Function<S, T> editor) {
		Objects.requireNonNull(editor, EDITOR_NOT_NULL);
		return new CaptureEditSpecification<>(path, editor, specification);
	}
	
	public <S extends JsonType, T extends JsonType>
	CaptureEditSpecification<JsonArray, JsonArray>
	editEach(final Function<S, T> editor) {
		Objects.requireNonNull(editor, EDITOR_NOT_NULL);
		
		@SuppressWarnings("unchecked")
		Function<JsonArray, JsonArray> eachEditor = array -> {
			JsonArray result = new JsonArray();
			array.forEach(value -> result.add(editor.apply((S) value)));
			return result;
		};
		
		return new CaptureEditSpecification<>(path, eachEditor, specification);
	}
	
	public void andSaveAs(final String variableName) {
		Objects.requireNonNull(variableName, "variableName must not be null");
		
		new CaptureEditSpecification<>(path, Function.identity(), specification)
		.andSaveAs(variableName);
	}

	public <S extends JsonType, T extends JsonType> void
	editAndReplace(final Function<S, T> editor) {
		Objects.requireNonNull(editor, EDITOR_NOT_NULL);
		
		specification.addChainedJsonVisitorSupplier(() -> {
			return new PathAwareJsonVisitor<Void>() {
				private JsonTreeBuildingVisitor treeBuilder = new JsonTreeBuildingVisitor();
				
				@Override
				protected boolean doBeforeVisitObject() {
					return passOn();
				}
				
				@Override
				protected boolean doBeforeVisitArray() {
					return passOn();
				}
				
				@Override
				protected Boolean doBeforeVisitValue(final boolean value) {
					return passOn() ? value : null;
				}
				
				@Override
				protected Number doBeforeVisitValue(final Number value) {
					return passOn() ? value : null;
				}
				
				@Override
				protected String doBeforeVisitValue(final String value) {
					return passOn() ? value : null;
				}
				
				@Override
				protected boolean doBeforeVisitNullValue() {
					return passOn();
				}
				
				private boolean passOn() {
					if (currentPath().matches(path)) {
						return false;
					} else {
						return true;
					}
				}
				
				@Override
				protected JsonObjectVisitor<Void> afterVisitObject(
						final JsonObjectVisitor<Void> visitor) {
					JsonObjectVisitor<Void> actualVisitor = visitor;
					
					if (currentPath().matches(path)) {
						MultiplexingJsonVisitor<Void> multiVisitor =
								new MultiplexingJsonVisitor<Void>(visitor, treeBuilder);
						
						actualVisitor = multiVisitor.visitObject();
					}
					
					return super.afterVisitObject(actualVisitor);
				}
				
				@Override
				protected void afterVisitObjectEnd() {
					handleAfterVisitEnd();
				}
				
				@Override
				protected JsonArrayVisitor<Void> afterVisitArray(
						final JsonArrayVisitor<Void> visitor) {
					JsonArrayVisitor<Void> actualVisitor = visitor;
					
					if (currentPath().matches(path)) {						
						MultiplexingJsonVisitor<Void> multiVisitor =
								new MultiplexingJsonVisitor<Void>(visitor, treeBuilder);
						
						actualVisitor = multiVisitor.visitArray();
					}
					
					return super.afterVisitArray(actualVisitor);
				}
				
				@Override
				protected void afterVisitArrayEnd() {
					handleAfterVisitEnd();
				}
				
				private void handleAfterVisitEnd() {
					if (currentPath().matches(path)) {
						handleAfterVisitValue((JsonType) treeBuilder.getVisitingResult());
					}
				}
				
				@Override
				protected void afterVisitValue(final Boolean value) {
					handleAfterVisitValue(new JsonBoolean(value));
				}
				
				@Override
				protected void afterVisitValue(final Number value) {
					handleAfterVisitValue(new JsonNumber(value));
				}
				
				@Override
				protected void afterVisitValue(final String value) {
					handleAfterVisitValue(new JsonString(value));
				}

				@SuppressWarnings("unchecked")
				private void handleAfterVisitValue(final JsonType jsonType) {
					JsonVisitor<Void> visitor = getNextVisitor();
					
					if (visitor != null && currentPath().matches(path)) {
						JsonType editedValue = editor.apply((S) jsonType);
						
						if (editedValue != null) {
							editedValue.mergeInto(visitor);
						} else {
							visitor.visitNullValue();
						}
					}
				}
			};
		});
	}
}