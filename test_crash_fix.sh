#!/bin/bash

echo "🔧 Testing ClassCastException Fix"
echo "================================="

echo "📁 Checking TaskViewHolder changes..."

# Check if CardView import is removed
if ! grep -q "import androidx.cardview.widget.CardView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ CardView import removed"
else
    echo "❌ CardView import still present"
fi

# Check if cardView field is replaced with taskView
if grep -q "public final View taskView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ taskView field declared correctly"
else
    echo "❌ taskView field not found"
fi

# Check if cardView cast is removed
if ! grep -q "cardView = (CardView) itemView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ CardView cast removed"
else
    echo "❌ CardView cast still present"
fi

# Check if taskView assignment is correct
if grep -q "taskView = itemView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    echo "✅ taskView assignment correct"
else
    echo "❌ taskView assignment not found"
fi

# Check if all cardView references are replaced
cardview_refs=$(grep -c "cardView\." app/src/main/java/com/newland/recents/views/TaskViewHolder.java)
if [ "$cardview_refs" -eq 0 ]; then
    echo "✅ All cardView references replaced"
else
    echo "❌ Found $cardview_refs remaining cardView references"
fi

# Check if taskView references exist
taskview_refs=$(grep -c "taskView\." app/src/main/java/com/newland/recents/views/TaskViewHolder.java)
if [ "$taskview_refs" -gt 0 ]; then
    echo "✅ Found $taskview_refs taskView references"
else
    echo "❌ No taskView references found"
fi

echo ""
echo "📱 Checking layout compatibility..."

# Check if layout uses FrameLayout
if grep -q "<FrameLayout" app/src/main/res/layout/task_item.xml; then
    echo "✅ Layout uses FrameLayout root"
else
    echo "❌ Layout doesn't use FrameLayout root"
fi

# Check if required views exist
required_views=("task_thumbnail" "task_icon" "task_title" "task_dismiss")
for view in "${required_views[@]}"; do
    if grep -q "android:id=\"@+id/$view\"" app/src/main/res/layout/task_item.xml; then
        echo "✅ View $view exists"
    else
        echo "❌ View $view missing"
    fi
done

echo ""
echo "🎯 Summary"
echo "=========="

# Count successful checks
total_checks=9
passed_checks=0

if ! grep -q "import androidx.cardview.widget.CardView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    ((passed_checks++))
fi

if grep -q "public final View taskView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    ((passed_checks++))
fi

if ! grep -q "cardView = (CardView) itemView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    ((passed_checks++))
fi

if grep -q "taskView = itemView" app/src/main/java/com/newland/recents/views/TaskViewHolder.java; then
    ((passed_checks++))
fi

cardview_refs=$(grep -c "cardView\." app/src/main/java/com/newland/recents/views/TaskViewHolder.java)
if [ "$cardview_refs" -eq 0 ]; then
    ((passed_checks++))
fi

taskview_refs=$(grep -c "taskView\." app/src/main/java/com/newland/recents/views/TaskViewHolder.java)
if [ "$taskview_refs" -gt 0 ]; then
    ((passed_checks++))
fi

if grep -q "<FrameLayout" app/src/main/res/layout/task_item.xml; then
    ((passed_checks++))
fi

# Check required views
view_count=0
for view in "${required_views[@]}"; do
    if grep -q "android:id=\"@+id/$view\"" app/src/main/res/layout/task_item.xml; then
        ((view_count++))
    fi
done

if [ "$view_count" -eq 4 ]; then
    ((passed_checks++))
    ((passed_checks++))
fi

echo "✅ Passed: $passed_checks/$total_checks checks"

percentage=$((passed_checks * 100 / total_checks))
echo "📈 Success Rate: $percentage%"

if [ $passed_checks -eq $total_checks ]; then
    echo ""
    echo "🎉 ClassCastException fix COMPLETE!"
    echo "   - CardView references removed ✅"
    echo "   - FrameLayout compatibility ✅"
    echo "   - All view references updated ✅"
else
    echo ""
    echo "⚠️  Some issues need attention"
fi

echo ""
echo "🚀 Ready for testing!"