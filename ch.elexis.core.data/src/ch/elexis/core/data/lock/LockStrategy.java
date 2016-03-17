package ch.elexis.core.data.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

/**
 * For first version we lock on patient, resp. whole domain so we should add all
 * dependendent locking elements
 *
 */
public class LockStrategy {

	public static List<LockInfo> createLockInfoList(Patient patient, String userId, String systemUuid) {
		ArrayList<LockInfo> lockList = new ArrayList<>();

		lockList.add(new LockInfo(patient.storeToString(), userId, systemUuid));

		List<LockInfo> bezugskontakte = patient.getBezugsKontakte().stream()
				.map(b -> new LockInfo(b.storeToString(), userId, systemUuid)).collect(Collectors.toList());
		lockList.addAll(bezugskontakte);

		List<LockInfo> faelle = Arrays.asList(patient.getFaelle()).stream()
				.map(l -> new LockInfo(l.storeToString(), userId, systemUuid)).collect(Collectors.toList());
		lockList.addAll(faelle);

		if (faelle.size() > 0) {
			Query<Konsultation> qbeKonsen = new Query<>(Konsultation.class);
			qbeKonsen.startGroup();
			for (LockInfo fall : faelle) {
				qbeKonsen.add(Konsultation.FLD_CASE_ID, Query.LIKE, fall.getElementId());
				qbeKonsen.or();
			}
			qbeKonsen.endGroup();
			List<LockInfo> konsen = qbeKonsen.execute().stream()
					.map(k -> new LockInfo(k.storeToString(), userId, systemUuid)).collect(Collectors.toList());
			lockList.addAll(konsen);
		}

		return lockList;
	}

	public static LockInfo createLockInfoList(String storeToString, String userId, String systemUuid) {
		return new LockInfo(storeToString, userId, systemUuid);
	}

}
