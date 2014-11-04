package com.evilgeniustechnologies.Wordrific.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.evilgeniustechnologies.Wordrific.activities.PlayActivity;
import com.evilgeniustechnologies.Wordrific.daomodel.Puzzle;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vendetta on 6/12/14.
 */
public class PuzzleAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private Context context;
    private ViewPager pager;
    private List<DawnUtilities.Tuple> puzzleList;
    private int puzzleIndex = 0;
    private boolean top = true;
    private boolean bottom = false;

    public PuzzleAdapter(Context context, ViewPager pager, List<Puzzle> puzzles) {
        this.context = context;
        this.pager = pager;
        L.e("puzzles size", puzzles.size());
        this.puzzleList = generatePuzzleList(puzzles);
        this.pager.setAdapter(this);
        this.pager.setOnPageChangeListener(this);
    }

    private List<DawnUtilities.Tuple> generatePuzzleList(List<Puzzle> puzzles) {
        Collections.sort(puzzles, new PuzzleComparator());
        List<DawnUtilities.Tuple> tuples = new ArrayList<DawnUtilities.Tuple>();
        for (Puzzle puzzle : puzzles) {
            tuples.add(new DawnUtilities.Tuple(puzzle, new PuzzleState(DawnUtilities.getClues(puzzle.getClues()), puzzle.getBonusClue())));
        }
        return tuples;
    }

    public DawnUtilities.Tuple getCurrentPuzzle() {
        return puzzleList.get(puzzleIndex);
    }

    public boolean skipPuzzle() {
        if (puzzleList.size() == 1) {
            return false;
        } else if (!bottom) {
            puzzleList.remove(puzzleIndex);
            notifyDataSetChanged();
            return true;
        } else if (!top) {
            puzzleList.remove(puzzleIndex);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    private void topOrBottom() {
        top = puzzleIndex == 0;
        bottom = puzzleIndex == puzzleList.size() - 1;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isBottom() {
        return bottom;
    }

    @Override
    public int getCount() {
        return puzzleList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Puzzle puzzle = (Puzzle) puzzleList.get(position).head;
        ImageView puzzleView = new ImageView(context);
        byte[] data = puzzle.getImage();
        puzzleView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        container.addView(puzzleView);
        return puzzleView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((ImageView) object));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        puzzleIndex = position;
        topOrBottom();
        ((PlayActivity) context).populateContent();
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public static class PuzzleState {
        private List<DawnUtilities.Tuple> clueList;
        private int clueIndex = -1;
        private boolean top = true;
        private boolean bottom = false;
        private boolean bonusEnabled = false;
        private boolean used = false;
        private long currentTime = 0;
        private DawnUtilities.Tuple bonus = null;

        public PuzzleState(List<String> clues, String bonusClue) {
            clueList = generateClueList(clues, bonusClue);
        }

        private List<DawnUtilities.Tuple> generateClueList(List<String> clues, String bonusClue) {
            List<DawnUtilities.Tuple> tuples = new ArrayList<DawnUtilities.Tuple>();
            if (!TextUtils.isEmpty(bonusClue)) {
                bonus = new DawnUtilities.Tuple(bonusClue, true);
            }
            for (String clue : clues) {
                tuples.add(new DawnUtilities.Tuple(clue, false));
            }
            return tuples;
        }

        public DawnUtilities.Tuple getCurrentClue() {
            if (bonusEnabled && !clueList.contains(bonus)) {
                clueList.add(0, bonus);
                clueIndex = 0;
                topOrBottom();
            }
            if (clueIndex >= 0 && clueIndex < clueList.size()) {
                return clueList.get(clueIndex);
            }
            return null;
        }

        public boolean isNextCluePurchased() {
            if (!bottom) {
                return (Boolean) clueList.get(clueIndex + 1).tail;
            }
            return (Boolean) clueList.get(clueIndex).tail;
        }

        public DawnUtilities.Tuple nextClue() {
            used = true;
            if (!bottom) {
                clueIndex++;
            }
            topOrBottom();
            return clueList.get(clueIndex);
        }

        public DawnUtilities.Tuple previousClue() {
            if (clueIndex < 0) {
                return null;
            }
            if (!top) {
                clueIndex--;
            }
            topOrBottom();
            return clueList.get(clueIndex);
        }

        private void topOrBottom() {
            top = clueIndex == 0;
            bottom = clueIndex == clueList.size() - 1;
        }

        public void enableBonus() {
            bonusEnabled = true;
        }

        public boolean isTop() {
            return top;
        }

        public boolean isBottom() {
            return bottom;
        }

        public boolean isUsed() {
            return used;
        }

        public long getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }
    }

    private static class PuzzleComparator implements Comparator<Puzzle> {

        @Override
        public int compare(Puzzle lhs, Puzzle rhs) {
            return lhs.getNumber().compareTo(rhs.getNumber());
        }
    }
}
