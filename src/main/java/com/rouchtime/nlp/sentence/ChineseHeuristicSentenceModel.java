package com.rouchtime.nlp.sentence;

import java.util.Collection;
import java.util.Set;

public class ChineseHeuristicSentenceModel extends ChineseAbstractSentenceModel {

	private final boolean mForceFinalStop;
	private final boolean mBalanceParens;
	Set<String> mPossibleStops;
	Set<String> mBadPrevious;
	Set<String> mBadFollowing;

	public ChineseHeuristicSentenceModel(Set<String> possibleStops, Set<String> impossiblePenultimate,
			Set<String> impossibleStarts) {
		this(possibleStops, impossiblePenultimate, impossibleStarts, false, false);
	}

	public ChineseHeuristicSentenceModel(Set<String> possibleStops, Set<String> impossiblePenultimate,
			Set<String> impossibleStarts, boolean forceFinalStop, boolean balanceParens) {
		mPossibleStops = possibleStops;
		mBadPrevious = impossiblePenultimate;
		mBadFollowing = impossibleStarts;
		mForceFinalStop = forceFinalStop;
		mBalanceParens = balanceParens;
	}

	@Override
	public void boundaryIndices(String[] tokens, int start, int length, Collection<Integer> indices) {
		if (length == 0)
			return;
		if (length == 1) {
			if (mForceFinalStop || mPossibleStops.contains(tokens[start].toLowerCase())) {
				indices.add(Integer.valueOf(start));
			}
			return;
		}

		boolean inParens = false;
		if (tokens[start].substring(0, 1).equals("("))
			inParens = true;
		boolean inBrackets = false;
		if (tokens[start].equals("["))
			inBrackets = true;
		boolean inChineseParens = false;
		if (tokens[start].equals("（"))
			inChineseParens = true;
		boolean inChineseBrackets = false;
		if (tokens[start].equals("【"))
			inChineseBrackets = true;
		boolean inBookTitle = false;
		if (tokens[start].equals("《"))
			inBookTitle = true;
		boolean inQuot = false;
		if (tokens[start].equals("“"))
			inQuot = true;
		boolean inSingleQuot = false;
		if (tokens[start].equals("‘"))
			inSingleQuot = true;

		int end = start + length - 1;
		for (int i = start + 1; i < end; ++i) {
			if (mBalanceParens) {
				if (tokens[i].equals("(")) {
					inParens = true;
					continue;
				}
				if (tokens[i].equals(")")) {
					inParens = false;
					continue;
				}
				if (tokens[i].equals("[")) {
					inBrackets = true;
					continue;
				}
				if (tokens[i].equals("]")) {
					inBrackets = false;
					continue;
				}
				if (tokens[i].equals("（")) {
					inChineseParens = true;
					continue;
				}
				if (tokens[i].equals("）")) {
					inChineseParens = false;
					continue;
				}

				if (tokens[i].equals("【")) {
					inChineseBrackets = true;
					continue;
				}
				if (tokens[i].equals("】")) {
					inChineseBrackets = false;
					continue;
				}

				if (tokens[i].equals("《")) {
					inBookTitle = true;
					continue;
				}
				if (tokens[i].equals("》")) {
					inBookTitle = false;
					continue;
				}

				if (tokens[i].equals("“")) {
					inQuot = true;
					continue;
				}
				/* 存在某某说：“xxx。” */
				if (tokens[i].equals("”")) {
					if (!mPossibleStops.contains(tokens[i - 1])) {
						inQuot = false;
						continue;
					} else {
						inQuot = false;
						if (mBadFollowing.contains(tokens[i + 1]))
							continue;
						indices.add(Integer.valueOf(i));
						continue;
					}
				}
				if (tokens[i].equals("‘")) {
					inSingleQuot = true;
					continue;
				}
				if (tokens[i].equals("’")) {
					inSingleQuot = false;
					continue;
				}

				if (inParens || inBrackets || inChineseParens || inChineseBrackets || inBookTitle || inQuot
						|| inSingleQuot)
					continue;
			}

			// 检查词是否是可能句子结束的标识
			if (!mPossibleStops.contains(tokens[i])) {
				continue;
			}
			if (mBadFollowing.contains(tokens[i + 1]))
				continue;
			indices.add(Integer.valueOf(i));
		}
		if (mForceFinalStop || (mPossibleStops.contains(tokens[end]) && !mBadPrevious.contains(tokens[end - 1])))
			indices.add(Integer.valueOf(end));
	}

}
