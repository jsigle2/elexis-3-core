package ch.elexis.core.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Verrechnet;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.util.ModelUtil;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.rgw.tools.Money;

public class Billed extends AbstractIdDeleteModelAdapter<Verrechnet>
		implements IdentifiableWithXid, IBilled {
	
	private ExtInfoHandler extInfoHandler;
	
	public Billed(Verrechnet entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public IBillable getBillable(){
		String billableClass = getEntity().getKlasse();
		String billableId = getEntity().getLeistungenCode();
		if (StringUtils.isNotBlank(billableClass) && StringUtils.isNotBlank(billableId)) {
			return (IBillable) StoreToStringServiceHolder.get()
				.loadFromString(billableClass + IStoreToStringContribution.DOUBLECOLON + billableId)
				.orElse(null);
		}
		return null;
	}
	
	@Override
	public void setBillable(IBillable value){
		Optional<String> storeToString = StoreToStringServiceHolder.get().storeToString(value);
		storeToString.ifPresent(s -> {
			String[] split = s.split(IStoreToStringContribution.DOUBLECOLON);
			if (split.length > 1) {
				getEntity().setKlasse(split[0]);
				getEntity().setLeistungenCode(split[1]);
			}
		});
	}
	
	@Override
	public IEncounter getEncounter(){
		if (getEntity().getBehandlung() != null) {
			return ModelUtil.getAdapter(getEntity().getBehandlung(), IEncounter.class);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setEncounter(IEncounter value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setBehandlung(((AbstractIdModelAdapter<Behandlung>) value).getEntity());
		} else if (value == null) {
			getEntity().setBehandlung(null);
		}
	}
	
	@Override
	public double getAmount(){
		if (getSecondaryScale() == 100) {
			return getEntity().getZahl();
		}
		return getSecondaryScale() / 100;
	}
	
	@Override
	public void setAmount(double value){
		if (value % 1 == 0) {
			// integer
			getEntity().setZahl((int) value);
			setSecondaryScale(100);
		} else {
			if (!isChangedPrice()) {
				// double
				getEntity().setZahl(1);
				int scale2 = (int) Math.round(value * 100);
				setSecondaryScale(scale2);
			} else {
				throw new IllegalStateException(
					"Can not set non integer amount if price was changed");
			}
		}
	}
	
	@Override
	public Money getPrice(){
		return new Money(getPoints()).multiply(getFactor());
	}
	
	@Override
	public void setPrice(Money value){
		if (isNonIntegerAmount()) {
			throw new IllegalStateException("Can not set price if non integer amount was set");
		} else {
			setExtInfo(Constants.FLD_EXT_CHANGEDPRICE, "true");
			setPoints(value.getCents());
			setSecondaryScale(100);
		}
	}
	
	@Override
	public Money getNetPrice(){
		return new Money(getEntity().getEk_kosten());
	}
	
	@Override
	public void setNetPrice(Money value){
		getEntity().setEk_kosten(value.getCents());
	}
	
	@Override
	public String getText(){
		return getEntity().getLeistungenText();
	}
	
	@Override
	public void setText(String value){
		getEntity().setLeistungenText(value);
		
	}
	
	@Override
	public int getPoints(){
		return getEntity().getVk_tp();
	}
	
	@Override
	public void setPoints(int value){
		getEntity().setVk_tp(value);
	}
	
	@Override
	public double getFactor(){
		String scaleString = getEntity().getVk_scale();
		if (scaleString != null && !scaleString.isEmpty()) {
			try {
				return Double.parseDouble(scaleString);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 1.0;
	}
	
	@Override
	public void setFactor(double value){
		getEntity().setVk_scale(Double.toString(value));
	}
	
	@Override
	public int getPrimaryScale(){
		return getEntity().getScale();
	}
	
	@Override
	public void setPrimaryScale(int value){
		getEntity().setScale(value);
	}
	
	@Override
	public int getSecondaryScale(){
		return getEntity().getScale2();
	}
	
	@Override
	public void setSecondaryScale(int value){
		getEntity().setScale2(value);
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
	public String getCode(){
		return getEntity().getLeistungenCode();
	}
	
	@Override
	public Money getTotal(){
		return getPrice().multiply(getPrimaryScale() / 100).multiply(getSecondaryScale() / 100)
			.multiply(getAmount());
	}
	
	@Override
	public boolean isChangedPrice(){
		Object changedPrice = getExtInfo(Constants.FLD_EXT_CHANGEDPRICE);
		if (changedPrice instanceof String) {
			return ((String) changedPrice).equalsIgnoreCase("true");
		} else if (changedPrice instanceof Boolean) {
			return (Boolean) changedPrice;
		}
		return false;
	}
	
	@Override
	public boolean isNonIntegerAmount(){
		if (isChangedPrice()) {
			return false;
		} else {
			return getSecondaryScale() != 100;
		}
	}
}
