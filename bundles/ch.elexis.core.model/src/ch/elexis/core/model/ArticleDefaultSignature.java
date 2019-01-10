package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.DefaultSignature;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.article.defaultsignature.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

public class ArticleDefaultSignature
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.DefaultSignature>
		implements IdentifiableWithXid, IArticleDefaultSignature {
	
	private ExtInfoHandler extInfoHandler;
	private LocalDate endDate;
	
	public ArticleDefaultSignature(DefaultSignature entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public String getAtcCode(){
		return getEntity().getAtccode();
	}

	@Override
	public void setAtcCode(String value){
		getEntity().setAtccode(value);
	}

	@Override
	public String getMorning(){
		return getEntity().getMorning();
	}

	@Override
	public void setMorning(String value){
		getEntity().setMorning(value);
	}

	@Override
	public String getNoon(){
		return getEntity().getNoon();
	}

	@Override
	public void setNoon(String value){
		getEntity().setNoon(value);
	}

	@Override
	public String getEvening(){
		return getEntity().getEvening();
	}

	@Override
	public void setEvening(String value){
		getEntity().setEvening(value);
	}

	@Override
	public String getNight(){
		return getEntity().getNight();
	}

	@Override
	public void setNight(String value){
		getEntity().setNight(value);
	}

	@Override
	public String getComment(){
		return getEntity().getComment();
	}

	@Override
	public void setComment(String value){
		getEntity().setComment(value);
	}
	
	@Override
	public void setArticle(IArticle article){
		String articleString = article.getGtin() + "$" + article.getCode() + "$"
			+ StoreToStringServiceHolder.getStoreToString(article);
		getEntity().setArticle(articleString);
	}
	
	@Override
	public String getFreeText(){
		return (String) getExtInfo(Constants.EXT_FLD_FREETEXT);
	}
	
	@Override
	public void setFreeText(String value){
		if (value == null) {
			value = "";
		}
		setExtInfo(Constants.EXT_FLD_FREETEXT, value);
	}
	
	@Override
	public EntryType getMedicationType(){
		String typeNumber = (String) getExtInfo(Constants.EXT_FLD_MEDICATIONTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}
	
	@Override
	public void setMedicationType(EntryType value){
		setExtInfo(Constants.EXT_FLD_MEDICATIONTYPE, Integer.toString(value.numericValue()));
	}
	
	@Override
	public EntryType getDisposalType(){
		String typeNumber = (String) getExtInfo(Constants.EXT_FLD_DISPOSALTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}
	
	@Override
	public void setDisposalType(EntryType value){
		setExtInfo(Constants.EXT_FLD_DISPOSALTYPE, Integer.toString(value.numericValue()));
	}
	
	@Override
	public boolean isAtc(){
		return (getAtcCode() != null && !getAtcCode().isEmpty());
	}
	
	@Override
	public String getSignatureAsDosisString(){
		String freeText = getFreeText();
		if (freeText != null && !freeText.isEmpty()) {
			return freeText;
		}
		
		String[] values = new String[] {
			getMorning(), getNoon(), getEvening(), getNight()
		};
		
		StringBuilder sb = new StringBuilder();
		if (signatureInfoExists(values)) {
			for (int i = 0; i < values.length; i++) {
				String string = values[i] == null || values[i].isEmpty() ? "0" : values[i];
				
				if (i > 0) {
					sb.append("-");
				}
				sb.append(string);
			}
		}
		return sb.toString();
	}
	
	private boolean signatureInfoExists(String[] values){
		for (String val : values) {
			if (val != null && !val.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public LocalDate getEndDate(){
		return endDate;
	}
	
	@Override
	public void setEndDate(LocalDate value){
		this.endDate = value;
	}
}
